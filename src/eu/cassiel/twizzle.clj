(ns eu.cassiel.twizzle
  "The Adventures of Twizzle, a simple timeline automation system."
  (:require [eu.cassiel.twizzle [interpolators :as it]]))

(defn initial
  "Initial system state. Takes an optional map of starting values for
   channels which, in front of any fades, we want to be other than zero,
   and an optional map of interpolation functions.

   We fold the interp functions into the channels."
  [& {:keys [init interp]}]
  (let [chans (reduce-kv (fn [m k v] (assoc m k {:fades nil
                                               :current v}))
                         nil
                         init)
        chans' (reduce-kv (fn [m k v]
                            (update-in m [k] assoc :interp (get interp k)))
                          chans
                          interp)]
    {;; Channels: map from chan-name to {:fades, :current, :interp}.
     :channels chans'
     :time 0}))

(defn apply-to-channels
  "Apply function to (fades, current, interp) of all channels."
  [state f]
  (update-in state
             [:channels]
             (partial reduce-kv (fn [m k v] (assoc m k (f v))) nil)))

(defn apply-fade
  "Apply a fade to a current value, return new current value (unchanged if fade in the future).

   Edge conditions tweaked to allow zero-length fades, as well as gate-style interpolators."
  [interp {:keys [start dur target]} ts current]
  (cond (> start ts)                    ; fade starts ahead of us
        current

        (>= ts (+ start dur))           ; fade is completely over
        target

        :else
        (interp current target (/ (- ts start) dur))))

(defn purge
  "Purge a channel; remove all expired fades, chasing them (and updating `:current`)
   as we go. Exit (without interpolating) if we end up with a fade in scope."
  [{:keys [fades current interp] :as channel} ts]
  (if (empty? fades)
    channel
    (let [[f & f'] fades]
      (cond (> (:start f) ts)
            channel

            (< (+ (:start f) (:dur f)) ts)
            (recur {:fades f'
                    :current (:target f)
                    :interp interp} ts)

            :else
            channel))))

(defn clear
  "Clear all fades from a channel. Any fade that's partially done at the current location is
   chased to its current value. (Done by purging to the current location, and then clearing
   the channel."
  [{:keys [time] :as state} ch]
  (update-in state
             [:channels ch]
             #(-> %
                  (purge time)
                  (assoc :current (sample-channel % time))
                  (dissoc :fades))))

(defn automate-at
  "Add an automation fade to a channel `ch`. The fade starts at time, `ts`,
   lasts for `dur` frames and fades from the current value to `target`.

   If this fade lies totally in front of (earlier than) the current
   timestamp, it'll be chased and removed; otherwise it'll be
   interpolated if it's in scope.

   Returns a new state.

   Don't overlap fades. Bad things will happen."
  [state ch start-ts dur target]
  (update-in state
             [:channels ch]
             (fn [{f :fades c :current i :interp}]
               (let [f' (sort-by :start (conj f {:start start-ts :dur dur :target target}))
                     ch' (purge {:fades f' :current c :interp i} (:time state))]
                 ch'))))

(defn automate-by
  "Start fade at an offset from current location."
  [state ch offset-ts dur target]
  (automate-at state ch (+ (:time state) offset-ts) dur target))

(defn locate
  "Change the location of this state to timestamp `ts`, returning a new state. Expired
   fades will be applied at their last points and purged (so winding time back again
   will not restore them)."
  [state ts]
  (-> state
      (assoc :time ts)
      (apply-to-channels #(purge % ts))))

(defn sample-channel
  "Sample a channel at `t`  Assume purged (i.e. no fades are completely in
   front of the sample point)."
  [{:keys [fades current interp]} t]
  (if (empty? fades)
    current
    (apply-fade (or interp it/interp-default)
                (first fades)
                t
                current)))

(defn sample
  "Sample a channel `ch` at the state's current time."
  [{:keys [channels time]} ch]
  (sample-channel (get channels ch) time))

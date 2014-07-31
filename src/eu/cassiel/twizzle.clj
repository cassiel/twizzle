(ns eu.cassiel.twizzle
  "The Adventures of Twizzle, a simple timeline automation system."
  (:require [eu.cassiel.twizzle [interpolators :as it]]))

(defn initial
  "Initial system state. Takes an optional map of starting values for channels
   which, in front of any fades, we want to be other than zero.

   Also folds the interp functions into the channels."
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
  "Apply function to (fades * current) of all channels."
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
   as we go."
  [{:keys [fades current interp] :as channel} ts]
  (if (empty? fades)
    channel
    (let [[f f'] fades]
      (cond (> (:start f) ts)
            channel

            (< (+ (:start f) (:dur f)) ts)
            (recur {:fades f'
                    :current (:target f)
                    :interp interp} ts)

            :else
            channel))))

(defn automate
  "Add an automation fade to a channel `ch`. The fade starts at time, `ts`,
   lasts for `dur` frames and fades from the current value to `target`.

   If this fade lies totally in front of the current timestamp, it'll be chased and
   removed; otherwise it'll be interpolated if it's in scope.

   Returns a new state.

   Don't overlap fades. Bad things will happen. (Actually, fades will be applied
   in increasing order of starting stamp.)"
  [state ch start-ts dur target]
  (update-in state
             [:channels ch]
             (fn [{f :fades c :current i :interp}]
               (let [f' (sort-by :start (conj f {:start start-ts :dur dur :target target}))
                     ch' (purge {:fades f' :current c :interp i} (:time state))]
                 ch'))))

(defn locate
  "Change the location of this state to timestamp `ts`, returning a new state. Expired
   fades will be applied at their last points and purged (so winding time back again
   will not restore them)."
  [state ts]
  (-> state
      (assoc :time ts)
      (apply-to-channels #(purge % ts))))

(defn sample
  "Sample a channel `ch` at the state's current time. Assume purged (i.e. no fades
   are completely in front of the sample point)."
  [{:keys [channels time]} ch]
  (let [{:keys [fades current interp]} (get channels ch)]
    (if (empty? fades)
      current
      (apply-fade (or interp it/interp-default)
                  (first fades)
                  time
                  current))))

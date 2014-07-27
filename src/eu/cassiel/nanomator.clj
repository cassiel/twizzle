(ns eu.cassiel.nanomator
  "Nano-automation system.")

(defn initial
  "Initial system state. Takes an optional map of starting values for channels
   which, at the earliest timestamp, we want to be other than zero."
  [& {:keys [init interp]}]
  {:interp interp
   ;; Sequences: map from chan-name to {:fades, :current}.
   :sequences (reduce-kv (fn [m k v] (assoc m k {:fades nil :current v}))
                         nil
                         init)
   :time 0})

(defn automate
  "Add an automation segment to a channel `ch`. The segment starts at time, `ts`,
   lasts for `dur` frames and fades from the current value to `target`. The
   target value will actually be hit at time `(+ ts dur 1)` (so `dur` is treated
   as a time measure, not a frame count).

   Returns a new state, with this segment added, even if we're notionally beyond
   the end of the segment: the state doesn't track the last-executed time.

   Don't overlap segments. Bad things will happen. (Actually, segments will be applied
   in increasing order of starting stamp.)"
  [state ch start-ts dur target]
  (update-in state
             [:sequences ch]
             (fn [seqs]
               (update-in seqs [:fades]
                          #(as-> % L
                                 (conj L {:start start-ts :dur dur :target target})
                                 (sort-by :start L))))))

(defn apply-to-fades
  "Apply function to fade lists of all channels"
  [state f]
  (update-in state
             [:sequences]
             (partial reduce-kv (fn [m k v] (assoc m k (update-in v [:fades] f))) nil)))

(defn apply-to-sequences
  "Apply function to sequences (fades * current) of all channels"
  [state f]
  (update-in state
             [:sequences]
             (partial reduce-kv (fn [m k v] (assoc m k (f v))) nil)))

(defn interp-default
  "Default interpolation. Treats first value of `nil` as `0`."
  [val-1 val-2 pos]
  (let [val-1 (or val-1 0)]
    (+ val-1 (* (- val-2 val-1) pos))))

(defn apply-fade
  "Apply a fade to a current value, return new current value (unchanged if fade in the future)."
  [{:keys [start dur target]} ts current]
  (cond (<= ts start)
        current

        (>= ts (+ start dur))
        target

        ;; duration 0? TESTME
        :else
        (interp-default current target (/ (- ts start) dur))))

(defn purge-and-sample
  "Purge a sequence to a timestamp, sample it. Return sequence * value.
   Note that the value might not be the same as the :current field; if we're
   part-way through a sequence, the field is still nailed to the start value."
  [{:keys [fades current] :as sequence} ts]
  (if (empty? fades)
    [sequence current]
    (let [[s s'] fades]
      (cond (> (:start s) ts)
            [sequence current]

            (< (+ (:start s) (:dur s)) ts)
            (recur {:fades s'
                    :current (:target s)} ts)

            :else
            [{:fades fades :current current}
             (apply-fade s ts current)]))))

(defn locate
  "Change the location of this state to timestamp `ts`, returning a new state. Expired
   fades will be applied at their last points and purged (so winding time back again
   will not restore them)."
  [state ts]
  (-> state
      (assoc :time ts)
      (apply-to-sequences #(first (purge-and-sample % ts)))))

(defn sample
  "Sample a channel `ch` at the state's current time."
  [{:keys [sequences time]} ch]
  (fnext (purge-and-sample (get sequences ch) time)))

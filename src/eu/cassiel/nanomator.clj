(ns eu.cassiel.nanomator
  "Nano-automation system.")

(defn initial
  "Initial system state. Takes an optional map of starting values for channels
   which, at the earliest timestamp, we want to be other than zero."
  [& {:keys [init] :or {init {}}}]
  {:current init
   :fade-sequences {}})

(defn automate
  "Add an automation segment to a channel `ch`. The segment starts at time, `ts`,
   lasts for `dur` frames and fades from the current value to `target`. The
   target value will actually be hit at time `(+ ts dur 1)` (so `dur` is treated
   as a time measure, not a frame count).

   Returns a new state, with this segment added, even if we're notionally beyond
   the end of the segment: the state doesn't track the last-executed time.

   Don't overlap segments. Bad things will happen. (Actually, segments will be applied
   in increasing order of starting stamp.)"
    [state ch ts dur target]
  )

(defn sample
  "Sample a channel `ch` at a time `ts`. If there are expired segments, the last one
   will be applied to determine the current channel value. Then, any in-scope segment
   will be interpolated.

   We return the interpolated value, but make no change to the state; it might make
   sense to [[purge]] the state from time to time."
  [{c :current} ch ts]
  (or (get c ch) 0))

(defn purge
  "Purge this state, catching up to time `ts`. "
  [state ts]
  )

`-*- mode: markdown; mode: visual-line; mode: adaptive-wrap-prefix; -*-`

# `nanomator` [![Build Status](https://secure.travis-ci.org/cassiel/nanomator.png)](http://travis-ci.org/cassiel/nanomator) [![Dependency Status](https://www.versioneye.com/user/projects/53d2a43b851c56dc68000231/badge.svg?style=flat)](https://www.versioneye.com/user/projects/53d2a43b851c56dc68000231)

## Introduction

A Clojure library for simple timeline-based automation, useful for animation systems.

An automation state maps named *channels* to time-varying values. Each channel can be populated with automation segments, each of which has a start time, duration and target value for the channel's value.

`nanomator` doesn't have a particular notion of time: it just uses integer counters which can be milliseconds (for realtime animation), frames (for rendering), or anything else.

By default, channel values are floats, but it's possible to attach interpolation functions to allow automation over arbitrary data values (for example, vectors of floats for RGB colour mixing).

## Usage

In `project.clj`:

```clojure
[eu.cassiel/nanomator "0.1.0-SNAPSHOT"]
```

In the code:

```clojure
(:require [eu.cassiel [nanomator :as nn]])
```

Create a new automation state with

```clojure
(nn/???)
```

## Documentation

The source documentation is [here](https://cassiel.github.io/nanomator).

## License

Copyright © 2014 Nick Rothwell.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

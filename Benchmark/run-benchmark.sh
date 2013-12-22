#!/bin/bash

cd build/classes

echo -n "Starting Benchmark at "
date

{ sleep $[60*$1] ; echo ; } | java benchmark.Benchmark $2 $3


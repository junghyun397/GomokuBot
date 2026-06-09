#!/bin/sh

cd native/mintaka || exit

cargo build --release -p rusty_renju_c -p rusty_renju_image

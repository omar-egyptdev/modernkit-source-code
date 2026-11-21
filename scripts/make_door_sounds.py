#!/usr/bin/env python3
"""Synthesizes the custom obsidian door open/close sounds as OGG Vorbis.

The user had no real recordings, so the sounds are generated from scratch with
numpy (no scipy needed - filtering is done in the frequency domain):

  open  - latch clack, deep sub thump, slow heavy stone-on-stone grind with a
          wobble, a low motion "whoosh", and a settle thump at the end
  close - sharp impact crack, deep body thump (64 Hz + octave), stone rumble
          tail, a faint glassy ping (obsidian accent) and a room-shaking sub

Output: mono 44.1 kHz, peak-normalized, OGG Vorbis. Minecraft plays .ogg
(vorbis) only, so that is the target format.

Run from repo root:  python3 scripts/make_door_sounds.py
"""
import numpy as np
import soundfile as sf
from pathlib import Path

SR = 44100
OUT = Path('src/main/resources/assets/modernkit/sounds/block/obsidian_door')
rng = np.random.default_rng(20261117)


def band_noise(n, lo, hi):
    """Band-limited noise via FFT with linear ramp edges."""
    x = rng.standard_normal(n)
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(n, 1 / SR)
    m = np.clip((f - lo * 0.6) / (lo * 0.6), 0, 1) * np.clip((hi * 1.4 - f) / (hi * 0.4), 0, 1)
    y = np.fft.irfft(X * m, n)
    return y / (np.abs(y).max() + 1e-9)


def tone(freq, dur, sweep_to=None):
    n = int(dur * SR)
    t = np.arange(n) / SR
    if sweep_to:
        f = np.linspace(freq, sweep_to, n)
        phase = 2 * np.pi * np.cumsum(f) / SR
        return np.sin(phase)
    return np.sin(2 * np.pi * freq * t)


def decay(dur, tau):
    return np.exp(-np.arange(int(dur * SR)) / (tau * SR))


def place(buf, start, sig, gain):
    i = int(start * SR)
    j = min(len(buf), i + len(sig))
    if i < len(buf):
        buf[i:j] += sig[:j - i] * gain


def finish(x, peak=0.88, fade_in=0.008, fade_out=0.06):
    x = x / (np.abs(x).max() + 1e-9) * peak
    n_in, n_out = int(fade_in * SR), int(fade_out * SR)
    x[:n_in] *= np.linspace(0, 1, n_in)
    x[-n_out:] *= np.linspace(1, 0, n_out)
    return x


def make_open():
    dur = 0.95
    n = int(dur * SR)
    buf = np.zeros(n)
    t = np.arange(n) / SR

    # deep sub thump as the door breaks free
    place(buf, 0.00, tone(55, 0.30) * decay(0.30, 0.10), 0.95)

    # latch clack
    clack = band_noise(int(0.05 * SR), 2200, 6500) * decay(0.05, 0.012)
    place(buf, 0.005, clack, 0.45)

    # slow heavy stone grind with a 2.7 Hz wobble
    grind = band_noise(n, 110, 900)
    wob = 0.68 + 0.32 * np.sin(2 * np.pi * 2.7 * t + 1.2)
    rise = np.clip(t / 0.22, 0, 1)
    tail = np.exp(-np.maximum(0, t - 0.25) / 0.42)
    place(buf, 0.02, grind * wob * rise * tail, 0.62)

    # low motion whoosh (door swinging)
    whoosh = tone(175, 0.62, sweep_to=92)
    wenv = np.sin(np.pi * np.linspace(0, 1, len(whoosh))) ** 1.5
    place(buf, 0.04, whoosh * wenv, 0.28)

    # settle thump as it reaches the wall
    place(buf, 0.74, tone(70, 0.20) * decay(0.20, 0.07), 0.42)
    place(buf, 0.74, band_noise(int(0.06 * SR), 500, 2500) * decay(0.06, 0.02), 0.22)

    return finish(buf)


def make_close():
    dur = 0.75
    n = int(dur * SR)
    buf = np.zeros(n)

    # impact crack
    crack = band_noise(int(0.012 * SR), 700, 7000) * decay(0.012, 0.005)
    place(buf, 0.000, crack, 0.80)

    # body thump + octave partial
    place(buf, 0.000, tone(64, 0.45) * decay(0.45, 0.17), 1.00)
    place(buf, 0.000, tone(128, 0.20) * decay(0.20, 0.06), 0.40)

    # stone rumble tail
    rumble = band_noise(n, 40, 380) * decay(dur, 0.22)
    place(buf, 0.002, rumble, 0.50)

    # faint glassy ping (obsidian accent)
    place(buf, 0.012, tone(2380, 0.09) * decay(0.09, 0.028), 0.10)

    # room-shaking sub
    place(buf, 0.004, tone(48, 0.50) * decay(0.50, 0.26), 0.30)

    return finish(buf, fade_out=0.09)


OUT.mkdir(parents=True, exist_ok=True)
for name, sig in (('open', make_open()), ('close', make_close())):
    path = OUT / f'{name}.ogg'
    sf.write(path, sig, SR, format='OGG', subtype='VORBIS')
    info = sf.info(path)
    print(f'{path}  {info.subtype} {info.samplerate}Hz ch={info.channels} {info.duration:.2f}s')

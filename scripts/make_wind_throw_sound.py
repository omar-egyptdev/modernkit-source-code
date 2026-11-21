#!/usr/bin/env python3
"""Synthesizes the custom Wind Charge throw sound as OGG Vorbis.

The user supplied the three burst sounds, but no throw sound, and the throw
event previously referenced the vanilla trident throw. This script generates
a quick airy "fwoof" - an accelerating air whoosh - so every wind charge
function is fully custom:

  - an upper air band (300-2400 Hz) that swells fast and decays
  - a lower air-mass band (120-600 Hz) slightly delayed and softer
  - a faint rising body tone (doppler-style 140-300 Hz sweep)

Output: mono 44.1 kHz, peak-normalized, OGG Vorbis. Played in game at
volume 0.5 with a random 0.9-1.1 pitch, so it is normalized a bit louder
(0.85) to survive the low volume.

Run from repo root:  python3 scripts/make_wind_throw_sound.py
"""
import numpy as np
import soundfile as sf
from pathlib import Path

SR = 44100
OUT = Path('src/main/resources/assets/modernkit/sounds/entity/wind_charge/wind_throw.ogg')
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


def swell(n, attack, tau):
    """Fast exponential swell into an exponential decay."""
    t = np.arange(n) / SR
    rise = np.clip(t / attack, 0, 1) ** 1.5
    fall = np.exp(-np.maximum(0, t - attack) / tau)
    return rise * fall


def finish(x, peak=0.85, fade_in=0.006, fade_out=0.07):
    x = x / (np.abs(x).max() + 1e-9) * peak
    n_in, n_out = int(fade_in * SR), int(fade_out * SR)
    x[:n_in] *= np.linspace(0, 1, n_in)
    x[-n_out:] *= np.linspace(1, 0, n_out)
    return x


def make_throw():
    dur = 0.50
    n = int(dur * SR)
    buf = np.zeros(n)

    # upper air band - the fast "fwoof" lead
    buf += band_noise(n, 300, 2400) * swell(n, 0.045, 0.14) * 0.95

    # lower air mass - slightly delayed, softer
    air = band_noise(n, 120, 600) * swell(n, 0.09, 0.20)
    shift = int(0.02 * SR)
    buf[shift:] += air[:n - shift] * 0.55

    # faint rising body tone - doppler-style acceleration
    body = tone(140, dur, sweep_to=300)
    buf += body * swell(n, 0.05, 0.16) * 0.12

    return finish(buf)


sf.write(OUT, make_throw(), SR, format='OGG', subtype='VORBIS')
info = sf.info(OUT)
print(f'{OUT}  {info.subtype} {info.samplerate}Hz ch={info.channels} {info.duration:.2f}s')

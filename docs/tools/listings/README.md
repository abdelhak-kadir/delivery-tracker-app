# Chapter 5 listing generator

`chapters/05-implementation.tex` is GENERATED. Edit `tmpl.tex` (prose) or `spec.py`
(which file/lines each listing shows), then regenerate:

    python3 docs/tools/listings/gen.py

Listings are copied verbatim from the repo (imports and trailing `//` comments dropped,
elisions marked `// ...`), so re-run this after any code change shown in chapter 5.

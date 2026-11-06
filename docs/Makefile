.PHONY: pdf clean
pdf:
	latexmk -pdf -interaction=nonstopmode -outdir=build main.tex
clean:
	rm -rf build
	rm -f *.aux *.log *.toc *.lof *.lot *.out *.bbl *.bcf *.blg *.run.xml *.fls *.fdb_latexmk *.synctex.gz *.tdo

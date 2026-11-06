import re, sys, textwrap
import os
S=os.path.dirname(os.path.abspath(__file__))+'/'
R=os.path.abspath(S+'../../..')+'/'
CM={'Java':'//','JavaScript':'//','bash':'#','yaml':'#','HCL':'#','Dockerfile':'#','Python':'#','Rego':'#','nginx':'#'}
LANG={'Java':'Java','JavaScript':'JavaScript','bash':'bash','yaml':'YAML','HCL':'HCL','Dockerfile':'Dockerfile','Python':'Python','Rego':'bash','nginx':'bash'}
def part(lines,start,end=None,n=None,after=0):
    i=None
    for k in range(after,len(lines)):
        if re.search(start,lines[k]): i=k;break
    if i is None: raise SystemExit('NOT FOUND '+start)
    if n is not None: j=i+n
    elif end is not None:
        for k in range(i,len(lines)):
            if re.search(end,lines[k]): j=k+1;break
        else: raise SystemExit('END NOT FOUND '+end)
    else: j=len(lines)
    return lines[i:j], j
def build(file,parts,lang,skipre=None):
    raw=open(R+file).read().expandtabs(2).split('\n')
    segs=[];after=0
    for p in parts:
        if isinstance(p,str):
            segs.append(('lit',p)); continue
        st,en,n=(tuple(p)+(None,None))[:3]
        seg,after=part(raw,st,en,n,after)
        segs.append(('seg',seg))
    allseg=[l for k,v in segs if k=='seg' for l in v if l.strip()]
    ind=min(len(l)-len(l.lstrip()) for l in allseg)
    res=[]
    for i,(k,v) in enumerate(segs):
        if k=='lit':
            res.append(CM[lang]+' ...' if v=='...' else v[1:]); continue
        if i>0 and segs[i-1][0]=='seg':
            first=next(l for l in v if l.strip())
            res.append(' '*(len(first)-len(first.lstrip())-ind+(4 if first.lstrip().startswith('}') else 0))+CM[lang]+' ...')
        v=[l[ind:].rstrip() for l in v]
        if skipre: v=[l for l in v if not re.search(skipre,l)]
        if lang in ('Java','JavaScript'): v=[re.sub(r'(\S)\s+//\s.*$',r'\1',l) for l in v]
        res+=v
    while res and not res[-1].strip(): res.pop()
    for l in res:
        if len(l)>94: print('LONG',len(l),file,l,file=sys.stderr)
        if any(ord(c)>127 for c in l): print('NONASCII',file,l,file=sys.stderr)
    return '\n'.join(res)
L={}
def add(key,file,parts,lang,caption,skipre=None):
    L[key]=(build(file,parts,lang,skipre),lang,caption,file)
exec(open(S+'spec.py').read())
tmpl=open(S+'tmpl.tex').read()
def rep(m):
    k=m.group(1); code,lang,cap,file=L[k]
    return '\\begin{lstlisting}[language=%s, basicstyle=\\ttfamily\\scriptsize, caption={%s (\\texttt{%s})}, label={lst:%s}]\n%s\n\\end{lstlisting}'%(LANG[lang],cap,file.replace('_','\\_').replace('/','/\\allowbreak '),k,code)
out=re.sub(r'@@LST:([a-z0-9-]+)@@',rep,tmpl)
open(R+'docs/chapters/05-implementation.tex','w').write(out)

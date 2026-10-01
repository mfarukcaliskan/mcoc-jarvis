import os, json, re, subprocess, difflib, sys, shutil, warnings
warnings.filterwarnings('ignore')
from PIL import Image, ImageOps
from cells import find_cells_labeled
TMP=os.path.join(os.environ['TEMP'],'guia','_ocr_in')
OURS=json.load(open('C:/Users/muhammed faruk/Desktop/JARVIS ASISTAN/JARVIS/app/src/main/assets/champions_db.json',encoding='utf-8'))
norm=lambda s:re.sub(r'[^a-z0-9]','',s.lower())
NAMES={}
for c in OURS:
    NAMES.setdefault(norm(c['name']),c['id'])
    # parantezsiz kisa ad (Hulk (Immortal) -> hulk immortal)
ALIASES={'ihulk':'hulkimmortal','iabom':'abominationimmortal','gobglin':'greengoblin','ggoblin':'greengoblin','stormx':'stormpyramidx','drvoodoo':'doctorvoodoo','drdoom':'doctordoom','spiderw':'spiderwoman','highevo':'highevolutionary','negative':'misternegative','cbritain':'captainbritain','ssamurai':'silversamurai','joefixit':'joefixit','antivenom':'antivenom','imiw':'ironmaninfinitywar'}
def run_ocr(crops):
    shutil.rmtree(TMP,ignore_errors=True); os.makedirs(TMP)
    for k,im in crops.items(): im.save(os.path.join(TMP,k+'.png'))
    out=os.path.join(TMP,'out.json')
    subprocess.run(['powershell','-NoProfile','-ExecutionPolicy','Bypass','-File','ocr_batch.ps1','-Dir',TMP,'-Out',out],capture_output=True,text=True)
    return json.load(open(out,encoding='utf-8-sig'))
def prep_label(im):
    im=im.convert('L'); w,h=im.size
    # beyaz yazi / koyu zemin -> OCR icin siyah yazi / beyaz zemin
    if sum(im.getdata())/(w*h)<128: im=ImageOps.invert(im)
    im=ImageOps.autocontrast(im)
    im=im.resize((w*3,h*3),Image.LANCZOS).point(lambda v:255 if v>150 else 0)
    return ImageOps.expand(im,border=30,fill=255).convert('RGB')
def resolve(text):
    t=norm(text)
    if not t: return []
    if t in ALIASES: return [(ALIASES[t],1.0)]
    if t in NAMES: return [(NAMES[t],1.0)]
    cands=difflib.get_close_matches(t,list(NAMES)+list(ALIASES),n=3,cutoff=0.6)
    out=[]
    for c in cands:
        out.append((ALIASES.get(c) or NAMES[c], difflib.SequenceMatcher(None,t,c).ratio()))
    return out
def ocr_cells(path):
    im,cells=find_cells_labeled(path)
    crops={f'c{i:03d}':prep_label(im.crop(c['label'])) for i,c in enumerate(cells) if c['label']}
    txt=run_ocr(crops)
    return im,cells,[txt.get(f'c{i:03d}.png','') for i in range(len(cells))]
if __name__=='__main__':
    sys.stdout.reconfigure(encoding='utf-8')
    im,cells,txt=ocr_cells(sys.argv[1])
    for t in txt: print(repr(t),resolve(t)[:2])

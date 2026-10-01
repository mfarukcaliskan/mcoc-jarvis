import numpy as np, warnings
from PIL import Image, ImageDraw
warnings.filterwarnings('ignore')
from match import S, load_refs
names,G,C=load_refs('refs')
def debg(im,thresh=36):
    im=im.convert('RGB').copy(); w,h=im.size
    for pt in [(0,0),(w-1,0),(0,h-1),(w-1,h-1),(w//2,0),(w//2,h-1),(0,h//2),(w-1,h//2)]:
        try: ImageDraw.floodfill(im,pt,(128,128,128),thresh=thresh)
        except Exception: pass
    return im
def feat_img(im,crop=True):
    im=im.convert('RGB'); w,h=im.size
    if crop: im=im.crop((int(w*0.08),int(h*0.04),int(w*0.92),int(h*0.78)))
    a=np.asarray(im.resize((S,S),Image.LANCZOS),dtype=np.float32)
    g=a.mean(axis=2); g=(g-g.mean())/(g.std()+1e-6)
    return g.ravel(),(a/255.0).ravel()
def match(im,use_debg=True):
    x=debg(im) if use_debg else im
    g,c=feat_img(x)
    d=np.sqrt(((G-g)**2).mean(axis=1))+2.0*np.sqrt(((C-c)**2).mean(axis=1))
    o=np.argsort(d)[:2]
    return names[o[0]],round(float(d[o[0]]),2),round(float(d[o[1]]-d[o[0]]),2)

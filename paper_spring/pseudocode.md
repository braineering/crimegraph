# LINK ANALYSIS

## PSEUDO-CODICE OPERATORE 1:
L'operatore 1 riceve un'interazione (x,y) ed emette coppie (a,b) di cui è necessario aggiornare lo score.

```
var T > = 2

func receive(x,y,w):
  if (x in G and y not in G):
    addArcInG(x,y,w)
    C = update(y)
    for (c in C):
      emit(c)
  else if (y in G and x not in G):
    addArcInG(x,y,w)
    C = update(x)
    for (c in C):
      emit(c)
  else if (x in G and y in G):
    addOrUpdateArcInG(x,y,w)
    C_x = update(x)
    C_y = update(y)
    C = C_x union C_y
    for (c in C):
      emit(c)
  else:
    addArcInG(x,y,w)
```

```
func update(x):
  Set<Node> S = N(x,T)
  Set<Arc> R = new Set<>()
  for (y in S):
    if ((x,y) not in G):
      R.add(x,y)
    for (z in S):
      if ((y,z) not in G and z != y):
        R.add(y,z)
  return R
```

```
func N(x,t):
  return insieme di nodi distanti al più t da x
```

## Metrica prediction
Locale, quasi-locale, quasi-locale pesata.
```
S(x,y) = \sum_{z \in H2_{G}(x) \cap H2_{G}(y)} \frac{1}{k_{z}} 

S(x,y,t) = \sum_{z \in N_{G}(x,i) \cap N_{G}(y,i)} \frac{1}{k_{z}}

S(x,y,t,a[t]) = \sum_{i=1}^{t}a_{i} \sum_{z \in N_{G}(x,i) \cap N_{G}(y,i)} \frac{1}{k_{z}}

## Metrica detection
```
S(x,y) = \sum_{z \in H2_{G}(x) \cap H2_{G}(y)} \frac{w_{x,z}+w_{z,y}}{k_{z}}

# LINK ANALYSIS

## PSEUDO-CODICE OPERATORE 1:
L'operatore 1 riceve un'interazione (x,y) ed emette coppie (a,b) di cui è necessario aggiornare lo score.

```
var T

func receive(x,y):
  if (x in G & y not in G):
    addArcInG(x,y)
    C = update(y)
    for (c in C):
      emit(c)
  else if (y in G & x not in G):
    addArcInG(x,y)
    C = update(x)
    for (c in C):
      emit(c)
  else if (x in G & y in G):
    addArcInG(x,y)
    C_x = update(x)
    C_y = update(y)
    C = C_x union C_y
    for (c in C):
      emit(c)
  else:
    addArcInG(x,y)
```

```
func update(x):
  Set<Node> S = N(x,T)
  Set<Arc> R = new Set<>()
  for (y in S):
    if ((x,y) not in G):
      R.add(x,y)
    for (z in S):
      if ((y,z) not in G  && z != y):
        R.add(y,z)
  return R
```

```
func N(x,t):
  return insieme di nodi distanti al più t da x
```

## Metrica
```
S(x,y,t,a[t]) = \sum_{i=1}^{t}a_{i} \sum_{z \in N_{G}(x,i) \cap N_{G}(y,i)} \frac{1}{k_{z}}
```

## Old
```
func update(x):
  Set<Node> S = H2(x)
  Set<Arc> R = new Set<>()
  for (y in S):
    R.add(x,y)
    for (z in S):
      if (z != y):
        R.add(y,z)
  for (a in R):
    emit(a)
```

```
func H2(x):
  Set<Node> C = x.getChildren()
  Set<Node> R = new Set<>()
  for (c in C):
    R.add(c.getChildren())
  return R
```

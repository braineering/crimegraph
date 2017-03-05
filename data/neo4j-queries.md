## DB reset
    MATCH n DETACH DELETE n
    CREATE CONSTRAINT ON (u:Person) IS UNIQUE n.id



## SAVE REAL LINK
    MERGE (u1:Person {id:{src}})
    MERGE (u2:Person {id:{dst}})
    MERGE (u1)-[r:REAL]-(u2)
    ON CREATE SET r.weight={weight},r.num=1,r.created=timestamp(),r.updated=r.created
    ON MATCH SET r.weight=(r.weight*r.num+{weight})/(r.num+1),r.num=r.num+1,r.updated=timestamp()


## SAVE POTENTIAL LINK
    MERGE (u1:Person {id:{src}})
    MERGE (u2:Person {id:{dst}})
    MERGE (u1)-[r:POTENTIAL]-(u2)
    ON CREATE SET r.weight={weight},r.created=timestamp(),r.updated=r.created
    ON MATCH SET r.weight={weight},r.updated=timestamp()


## SAVE HIDDEN LINK
    MERGE (u1:Person {id:{src}})
    MERGE (u2:Person {id:{dst}})
    MERGE (u1)-[r:HIDDEN]-(u2)
    ON CREATE SET r.weight={weight},r.created=timestamp(),r.updated=r.created
    ON MATCH SET r.weight={weight},r.updated=timestamp()


## NEIGHBOURS
    MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)
    RETURN DISTINCT n.id AS id


## NEIGHBOURS WITH DEGREE
    MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)
    WITH DISTINCT n
    MATCH (n)-[r:REAL]-()
    RETURN n.id AS id,COUNT(r) AS deg


## NEIGHBOURS WHITIN DISTANCE
    MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)
    RETURN DISTINCT n.id AS id


## NEIGHBOURS WITH DEGREE WITHIN DISTANCE
    MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)
    WITH DISTINCT n
    MATCH (n)-[r:REAL]-()
    RETURN n.id AS id,COUNT(r) AS deg


## COMMON NEIGHBOURS
    MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)-[:REAL]-(u2:Person {id:{dst}})
    RETURN n.id AS id


## COMMON NEIGHBOURS WITH DEGREE
    MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)-[:REAL]-(u2:Person {id:{dst}})
    WITH DISTINCT n
    MATCH (n)-[r:REAL]-()
    RETURN n.id AS id,COUNT(r) AS deg


## COMMON NEIGHBOURS WHITIN DISTANCE
    MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)-[:REAL*1..%d]-(u2:Person {id:{dst}})
    RETURN DISTINCT n.id AS id


## COMMON NEIGHBOURS WITH DEGREE WITHIN DISTANCE
    MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)-[:REAL*1..%d]-(u2:Person {id:{dst}})
    WITH DISTINCT n
    MATCH (n)-[r:REAL]-()
    RETURN n.id AS id,COUNT(r) AS deg


## CHECK EXTREMES
    OPTIONAL MATCH (u1:Person {id:{src}})
    OPTIONAL MATCH (u2:Person {id:{dst}})
    WITH u1,u2
    OPTIONAL MATCH (u1)-[r:REAL]-(u2)
    RETURN u1 IS NOT NULL AS src,u2 IS NOT NULL AS dst,r IS NOT NULL AS arc


## NODES TO UPDATE                        
dato nodo x, restituisce la lista di archi (a,b) non in G, dove a e b sono nodi vicini di x.
    
    MATCH (n1:Person)-[:REAL]-(u:Person {id:{x}})-[:REAL]-(n2:Person)
    WHERE id(n1) > id(n2)
    RETURN collect([n1.id,n2.id]) as pairs


## UPDATE TWICE                  
dato nodo x, restituisce l'insieme Cx di archi (x,b) e (a,b) non in G, dove a e b sono nodi vicini di x.
dato nodo y, restituisce l'insieme Cy di archi (y,b) e (a,b) non in G, dove a e b sono nodi vicini di y.
restituisce l'unione di Cx e Cy

    MATCH (n1:Person)-[:REAL]-(u1:Person {id:{x}})-[:REAL]-(n2:Person)
    WHERE id(n1) > id(n2)
    RETURN collect([n1.id,n2.id]) as pairs
    UNION ALL
    MATCH (n3:Person)-[:REAL]-(u2:Person {id:{y}})-[:REAL]-(n4:Person)
    WHERE id(n3) > id(n4)
    RETURN collect([n3.id,n4.id]) as pairs


## UPDATE DISTANCE T             
dato nodo x, restituisce la lista di archi (x,b) e (a,b) non in G, dove a e b sono nodi a distanza t da x.

    MATCH (n1:Person)-[:REAL*1..%d]-(u:Person {id:{x}})-[:REAL*1..%d]-(n2:Person)
    WHERE id(n1) > id(n2)
    RETURN collect([n1.id,n2.id]) as pairs


## UPDATE TWICE DISTANCE T       
dato nodo x, restituisce l'insieme Cx di archi (x,b) e (a,b) non in G, dove a e b sono nodi a distanza t da x.
dato nodo y, restituisce l'insieme Cy di archi (y,b) e (a,b) non in G, dove a e b sono nodi a distanza t da y.
restituisce l'unione di Cx e Cy

    MATCH (n1:Person)-[:REAL*1..%d]-(u:Person {id:{x}})-[:REAL*1..%d]-(n2:Person)
    WHERE id(n1) > id(n2)
    RETURN collect([n1.id,n2.id]) as pairs
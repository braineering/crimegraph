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
    MATCH (x:Person {id:{x}})-[:REAL*2]-(n:Person)
    WHERE NOT (x)-[:REAL]-(n)
    RETURN [x.id,n.id] AS pairs
    UNION ALL
    MATCH (n1:Person)-[:REAL*2]-(x:Person {id:{x}})-[:REAL*2]-(n2:Person)
    WHERE id(n1) > id(n2) AND NOT (x)-[:REAL]-(n1) AND NOT (x)-[:REAL]-(n2)
    RETURN [n1.id,n2.id] as pairs


## UPDATE TWICE                 
    MATCH (x:Person {id:{x}})-[:REAL*2]-(n:Person)
    WHERE NOT (x)-[:REAL]-(n)
    RETURN [x.id,n.id] AS pairs
    UNION ALL
    MATCH (n1:Person)-[:REAL*2]-(x:Person {id:{x}})-[:REAL*2]-(n2:Person)
    WHERE id(n1) > id(n2) AND NOT (x)-[:REAL]-(n1) AND NOT (x)-[:REAL]-(n2)
    RETURN [n1.id,n2.id] as pairs
    UNION ALL
    MATCH (y:Person {id:{y}})-[:REAL*2]-(n:Person)
    WHERE NOT (y)-[:REAL]-(n)
    RETURN [y.id,n.id] AS pairs
    UNION ALL
    MATCH (n1:Person)-[:REAL*2]-(y:Person {id:{y}})-[:REAL*2]-(n2:Person)
    WHERE id(n1) > id(n2) AND NOT (y)-[:REAL]-(n1) AND NOT (y)-[:REAL]-(n2)
    RETURN [n1.id,n2.id] as pairs
    
## NODES TO UPDATE WITHIN DISTANCE 
    MATCH (x:Person {id:{x}})-[:REAL*%d]-(n:Person)
    WHERE NOT (x)-[:REAL*%d]-(n)
    RETURN [x.id,n.id] AS pairs
    UNION ALL
    MATCH (n1:Person)-[:REAL*%d]-(x:Person {id:{x}})-[:REAL*%d]-(n2:Person)
    WHERE id(n1) > id(n2) AND NOT (x)-[:REAL*%d]-(n1) AND NOT (x)-[:REAL*%d]-(n2)
    RETURN [n1.id,n2.id] as pairs


## UPDATE TWICE WITHIN DISTANCE
    MATCH (x:Person {id:{x}})-[:REAL*%d]-(n:Person)
    WHERE NOT (x)-[:REAL*%d]-(n)
    RETURN [x.id,n.id] AS pairs
    UNION ALL
    MATCH (n1:Person)-[:REAL*%d]-(x:Person {id:{x}})-[:REAL*%d]-(n2:Person)
    WHERE id(n1) > id(n2) AND NOT (x)-[:REAL*%d]-(n1) AND NOT (x)-[:REAL*%d]-(n2)
    RETURN [n1.id,n2.id] as pairs
    UNION ALL
    MATCH (y:Person {id:{y}})-[:REAL*%d]-(n:Person)
    WHERE NOT (y)-[:REAL*%d]-(n)
    RETURN [y.id,n.id] AS pairs
    UNION ALL
    MATCH (n1:Person)-[:REAL*%d]-(y:Person {id:{y}})-[:REAL*%d]-(n2:Person)
    WHERE id(n1) > id(n2) AND NOT (y)-[:REAL*%d]-(n1) AND NOT (y)-[:REAL*%d]-(n2)
    RETURN [n1.id,n2.id] as pairs
    
## POTENTIAL/HIDDEN LINK FORMULA
    MATCH (u1:Person {id:{x}})-[r1:REAL]-(n:Person)-[r2:REAL]-(u2:Person {id:{y}})
    WITH DISTINCT n,r1,r2
    MATCH (n)-[r:REAL]-()
    RETURN n.id AS id,COUNT(r) AS deg, (r1.weight+r2.weight) AS weight

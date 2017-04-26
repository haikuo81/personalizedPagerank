CREATE TABLE GRAPHS
(
	name VARCHAR(50) PRIMARY KEY,
	vertices INTEGER CHECK (vertices >= 0) NOT NULL,
	edges INTEGER CHECK (edges >= 0) NOT NULL,
	directed BOOLEAN,
	bipartite BOOLEAN
);



CREATE TABLE ALGORITHMS
(
	name VARCHAR(50) PRIMARY KEY,
	numberOfParameters smallint CHECK (numberOfParameters >= 0) NOT NULL
);

CREATE TABLE RUNS
(
	graph VARCHAR(50) REFERENCES GRAPHS,
	algorithm VARCHAR(50) REFERENCES ALGORITHMS,
	cpu VARCHAR(50),
	sampleNodes integer CHECK (sampleNodes > 0),
	topK integer CHECK (topK > 0),
	params double precision[],
	jaccardAverage smallInt CHECK (jaccardAverage >= 0 and jaccardAverage <= 100),
	jaccardMin double precision CHECK (jaccardMin >= 0.0),
	jaccardStd double precision CHECK (jaccardStd >= 0.0),
	kendallAverage double precision,
	kendallMin double precision,
	kendallStd double precision,
	runTime integer CHECK (runTime >= 0) NOT NULL,
	PRIMARY KEY (graph, algorithm, cpu, topK, jaccardAverage)
);

CREATE INDEX ON RUNS(lower(graph));
CREATE INDEX ON RUNS(lower(algorithm));
CREATE INDEX ON RUNS(lower(cpu));

CREATE OR REPLACE FUNCTION checkParamNumbers()
RETURNS trigger AS
$$
BEGIN
IF array_length(new.params, 1) = (select numberOfParameters from ALGORITHMS where name = new.algorithm) THEN
 RETURN NEW;
ELSE 
  RAISE EXCEPTION 'parameters length not matching between input and algorithms table';
END IF;
END;
$$
LANGUAGE 'plpgsql';


CREATE TRIGGER trigger_param_numbers 
BEFORE INSERT ON RUNS
FOR EACH ROW
EXECUTE PROCEDURE checkParamNumbers();


   





CREATE TABLE GRAPHS
(
	name VARCHAR(50) PRIMARY KEY,
	vertices INTEGER NOT NULL,
	edges INTEGER NOT NULL,
	directed BOOLEAN,
	bipartite BOOLEAN
  CHECK (vertices >=0),
  CHECK (edges >= 0)
);


CREATE TABLE ALGORITHMS
(
	name VARCHAR(50) PRIMARY KEY,
	params varchar(500) NOT NULL
);

CREATE TABLE RUNS
(
  id serial PRIMARY KEY,
	graph VARCHAR(50) REFERENCES GRAPHS,
	algorithm VARCHAR(50) REFERENCES ALGORITHMS,
	cpu VARCHAR(50),
	sampleNodes integer,
	topK integer,
	params varchar(200),
	jaccardAverage smallInt,
	jaccardMin double precision,
	jaccardStd double precision,
	kendallAverage smallInt,
	kendallMin double precision,
	kendallStd double precision,
	runTime integer,
  CHECK (sampleNodes > 0),
  CHECK (topK > 0),
  CHECK (jaccardAverage >= 0 and jaccardAverage <= 100),
  CHECK (jaccardMin >= 0.0 and jaccardMin <= 1.0),
  CHECK (jaccardStd >= 0.0),
  CHECK (kendallAverage >= -100 and kendallAverage <= 100),
  CHECK (kendallMin >= -1.0 and kendallMin <= 1.0),
  CHECK (kendallStd >= 0.0),
  CHECK (runTime >= 0)
);

CREATE INDEX graph_runs ON RUNS(graph);
CREATE INDEX algorithm_runs ON RUNS(algorithm);
CREATE INDEX cpu_runs ON RUNS(cpu);
CREATE INDEX runTime_runs ON RUNS(runTime);


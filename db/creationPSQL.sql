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
	params varchar(500) NOT NULL
);

CREATE TABLE RUNS
(
        id serial PRIMARY KEY,
	graph VARCHAR(50) REFERENCES GRAPHS,
	algorithm VARCHAR(50) REFERENCES ALGORITHMS,
	cpu VARCHAR(50),
	sampleNodes integer CHECK (sampleNodes > 0),
	topK integer CHECK (topK > 0),
	params varchar(200),
	jaccardAverage smallInt CHECK (jaccardAverage >= 0 and jaccardAverage <= 100),
	jaccardMin double precision CHECK (jaccardMin >= 0.0 and jaccardMin <= 1.0),
	jaccardStd double precision CHECK (jaccardStd >= 0.0),
	kendallAverage smallInt CHECK (kendallAverage >= -100 and kendallAverage <= 100),
	kendallMin double precision CHECK(kendallMin >= -1.0 and kendallMin <= 1.0),
	kendallStd double precision CHECK (kendallStd >= 0.0),
	runTime integer CHECK (runTime >= 0) NOT NULL
);

CREATE INDEX ON RUNS(lower(graph));
CREATE INDEX ON RUNS(lower(algorithm));
CREATE INDEX ON RUNS(lower(cpu));
CREATE INDEX runTime_runs ON RUNS(runTime);

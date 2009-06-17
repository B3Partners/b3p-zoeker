CREATE TABLE bron
(
  id serial NOT NULL,
  naam character varying(255),
  url character varying(255),
  volgorde integer,
  gebruikersnaam character varying(255),
  wachtwoord character varying(255),
  CONSTRAINT bron_pkey PRIMARY KEY (id)
)
WITH (OIDS=FALSE);

CREATE TABLE zoekconfiguratie
(
  id serial NOT NULL,
  naam character varying(255),
  featuretype character varying(255),
  parentbron integer,
  parentzoekconfiguratie integer,
  CONSTRAINT zoekconfiguratie_pkey PRIMARY KEY (id),
  CONSTRAINT fk88b2ec8965b04b72 FOREIGN KEY (parentbron)
      REFERENCES bron (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk88b2ec89801aee46 FOREIGN KEY (parentzoekconfiguratie)
      REFERENCES zoekconfiguratie (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);

CREATE TABLE resultaatveld
(
  id serial NOT NULL,
  label character varying(255),
  attribuutnaam character varying(255),
  "type" integer,
  zoekconfiguratie integer,
  naam character varying(255),
  CONSTRAINT resultaatveld_pkey PRIMARY KEY (id),
  CONSTRAINT fk1dffa7fe91b2799c FOREIGN KEY (zoekconfiguratie)
      REFERENCES zoekconfiguratie (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);

CREATE TABLE zoekveld
(
  id serial NOT NULL,
  label character varying(255),
  attribuutnaam character varying(255),
  zoekconfiguratie integer,
  "type" integer,
  volgorde integer,
  naam character varying,
  CONSTRAINT zoekveld_pkey PRIMARY KEY (id),
  CONSTRAINT fk2397898291b2799c FOREIGN KEY (zoekconfiguratie)
      REFERENCES zoekconfiguratie (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
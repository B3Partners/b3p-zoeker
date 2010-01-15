INSERT INTO bron (id,naam, url, volgorde, gebruikersnaam, wachtwoord) VALUES (1,'nlrpp', 'http://afnemers.ruimtelijkeplannen.nl/afnemers/services?Version=1.0.0', 1, NULL, NULL);

INSERT INTO zoekconfiguratie (id,naam, featuretype, parentbron, parentzoekconfiguratie) VALUES (1,'', 'app:Plangebied', 1, NULL);

INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'plan naam', 'naam', 2, 1, 'plannaam', NULL);
INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'plan id', 'identificatie', 1, 1, 'planid', NULL);
INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'documenten', 'verwijzingNaarTekst', 0, 1, 'documenten', NULL);
INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'plantype', 'typePlan', 0, 1, 'plantype', NULL);
INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'planstatus', 'planstatus', 0, 1, 'planstatus', NULL);
INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'geometry', 'geometrie', 3, 1, 'geometry', NULL);

INSERT INTO zoekveld (id,label, attribuutnaam, zoekconfiguratie, type, volgorde, naam) VALUES (zoekveld_id_seq.NEXTVAL,'Plannen', 'app:overheidscode', 1, NULL, NULL, 'plannen');

INSERT INTO bron (id, naam, url, volgorde, gebruikersnaam, wachtwoord) VALUES (2, 'local', 'jdbc:postgresql://vulhierjehostin:5432/vulhierjedatabasein.ennadepunthetschema', 2, 'vulhierdeusernamein', 'vulhierhetwachtwoordin');
INSERT INTO zoekconfiguratie (id, naam, featuretype, parentbron, parentzoekconfiguratie) VALUES (3, '', 'PLANEIGENAAR', 2, NULL);
INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'code', 'CODE', 1, 3, 'code', NULL);
INSERT INTO resultaatveld (id,label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES (resultaatveld_id_seq.NEXTVAL,'naam', 'NAAM', 2, 3, 'naam', NULL);
INSERT INTO zoekveld (id,label, attribuutnaam, zoekconfiguratie, type, volgorde, naam) VALUES (zoekveld_id_seq.NEXTVAL,'Eigenaar', 'NAAM', 3, NULL, NULL, 'eigenaar');

DROP SEQUENCE bron_id_seq;
CREATE SEQUENCE bron_id_seq start with 3;

DROP SEQUENCE zoekconfiguratie_id_seq;
CREATE SEQUENCE zoekconfiguratie_id_seq start with 4;

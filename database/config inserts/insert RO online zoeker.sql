INSERT INTO bron (id,naam, url, volgorde, gebruikersnaam, wachtwoord) VALUES (1,'nlrpp', 'http://afnemers.ruimtelijkeplannen.nl/afnemers/services?Version=1.0.0', 1, NULL, NULL);

INSERT INTO zoekconfiguratie (id,naam, featuretype, parentbron, parentzoekconfiguratie) VALUES (1,'', 'app:Plangebied', 1, NULL);

INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('plan naam', 'naam', 2, 1, 'plannaam', NULL);
INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('plan id', 'identificatie', 1, 1, 'planid', NULL);
INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('documenten', 'verwijzingNaarTekst', 0, 1, 'documenten', NULL);
INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('plantype', 'typePlan', 0, 1, 'plantype', NULL);
INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('planstatus', 'planstatus', 0, 1, 'planstatus', NULL);
INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('geometry', 'geometrie', 3, 1, 'geometry', NULL);

INSERT INTO zoekveld (label, attribuutnaam, zoekconfiguratie, type, volgorde, naam) VALUES ('Plannen', 'app:overheidscode', 1, NULL, NULL, 'plannen');

INSERT INTO bron (id, naam, url, volgorde, gebruikersnaam, wachtwoord) VALUES (2, 'local', 'jdbc:postgresql://vulhierjehostin:5432/vulhierjedatabasein.ennadepunthetschema', 2, 'vulhierdeusernamein', 'vulhierhetwachtwoordin');
INSERT INTO zoekconfiguratie (id, naam, featuretype, parentbron, parentzoekconfiguratie) VALUES (3, '', 'planeigenaar', 2, NULL);
INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('code', 'code', 1, 3, 'code', NULL);
INSERT INTO resultaatveld (label, attribuutnaam, type, zoekconfiguratie, naam, volgorde) VALUES ('naam', 'naam', 2, 3, 'naam', NULL);
INSERT INTO zoekveld (label, attribuutnaam, zoekconfiguratie, type, volgorde, naam) VALUES ('Eigenaar', 'naam', 3, NULL, NULL, 'eigenaar');

select setval('bron_id_seq', (select max(id) from bron)); 
select setval('zoekconfiguratie_id_seq', (select max(id) from zoekconfiguratie)); 
select setval('resultaatveld_id_seq', (select max(id) from resultaatveld)); 
select setval('zoekveld_id_seq', (select max(id) from zoekveld)); 
select setval('resultaatveld_id_seq', (select max(id) from resultaatveld)); 
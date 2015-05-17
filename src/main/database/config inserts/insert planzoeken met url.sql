INSERT INTO bron (naam,url)
	VALUES ('nlrpp','http://afnemers.ruimtelijkeplannen.nl/afnemers/services?Version=1.0.0');

INSERT INTO zoekconfiguratie(
            naam, featuretype, parentbron)
    VALUES ('planzoeken', 'app:Plangebied', (select max(id) from bron));

INSERT INTO zoekveld(
            naam, label, attribuutnaam, zoekconfiguratie)
    VALUES ('opPlanId', 'Plan', 'app:identificatie', (select max(id) from zoekconfiguratie));

INSERT INTO resultaatveld(naam,label,attribuutnaam,"type",zoekconfiguratie)
	VALUES('geometry','geometry','geometrie',3,(select max(id) from zoekconfiguratie));
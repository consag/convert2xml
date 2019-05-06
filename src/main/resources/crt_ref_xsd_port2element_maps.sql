CREATE TABLE REF_XSD_PORT2ELEMENT_MAPS
(XSD VARCHAR2(1000 CHAR)
,RootElement VARCHAR2(100 CHAR)
,Level1Element VARCHAR2(100 CHAR)
,ValuePortNr NUMBER(2)
,XMLElementName VARCHAR2(100 CHAR)
);
INSERT INTO ref_xsd_port2element_maps
VALUES
('t_risk_rating.xsd','table','T_RISK_RATING',1,'rating_type');
INSERT INTO ref_xsd_port2element_maps
VALUES
('t_risk_rating.xsd','table','T_RISK_RATING',2,'rating');
INSERT INTO ref_xsd_port2element_maps
VALUES
('t_risk_rating.xsd','table','T_RISK_RATING',3,'description');
INSERT INTO ref_xsd_port2element_maps
VALUES
('t_risk_rating.xsd','table','T_RISK_RATING',4,'is_rating_unrated');
INSERT INTO ref_xsd_port2element_maps
VALUES
('t_risk_rating.xsd','table','T_RISK_RATING',5,'quality_ascending_sequence');
INSERT INTO ref_xsd_port2element_maps
VALUES
('t_risk_rating.xsd','table','T_RISK_RATING',6,'rating_rank');
commit;


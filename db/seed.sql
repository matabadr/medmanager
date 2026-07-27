-- Services
INSERT INTO service_hospitalier (nom, type, capacite_lits) VALUES
  ('Cardiologie',  'CLINIQUE',     24),
  ('Pédiatrie',    'CLINIQUE',     30),
  ('Urgences',     'CLINIQUE',     12),
  ('Chirurgie',    'CLINIQUE',     18),
  ('Radiologie',   'TECHNIQUE',     0),
  ('Laboratoire',  'TECHNIQUE',     0),
  ('Administration','ADMINISTRATIF', 0);

-- Patients
INSERT INTO patient (id, nom, prenom, date_naissance, groupe_sanguin, telephone, service_nom) VALUES
  ('P001', 'Bennani',    'Sara',      '1992-03-14', 'A+',  '0612345678', 'Cardiologie'),
  ('P002', 'El Amrani',  'Youssef',   '1985-07-22', 'O-',  '0623456789', 'Urgences'),
  ('P003', 'Tazi',       'Leila',     '2014-11-05', 'B+',  '0634567890', 'Pédiatrie'),
  ('P004', 'Berrada',    'Khalid',    '1970-01-30', 'AB+', '0645678901', 'Chirurgie'),
  ('P005', 'Idrissi',    'Nour',      '1999-09-09', 'A-',  '0656789012', 'Cardiologie'),
  ('P006', 'Cherkaoui',  'Imane',     '2001-05-17', 'O+',  '0667890123', NULL),
  ('P007', 'Lahlou',     'Mehdi',     '1988-12-02', 'B-',  '0678901234', 'Urgences');

-- Médecins
INSERT INTO medecin (id, nom, prenom, date_naissance, specialite, matricule, telephone, service_nom) VALUES
  ('M001', 'Saidi',     'Rachid',  '1972-04-10', 'CARDIOLOGIE', 'MED-1001', '0611111111', 'Cardiologie'),
  ('M002', 'Bouzid',    'Fatima',  '1980-08-22', 'PEDIATRIE',   'MED-1002', '0622222222', 'Pédiatrie'),
  ('M003', 'Hakimi',    'Amine',   '1975-11-30', 'CHIRURGIE',   'MED-1003', '0633333333', 'Chirurgie'),
  ('M004', 'Naciri',    'Zineb',   '1983-02-15', 'URGENCES',    'MED-1004', '0644444444', 'Urgences'),
  ('M005', 'Belmokhtar','Hicham',  '1978-06-05', 'RADIOLOGIE',  'MED-1005', '0655555555', 'Radiologie');

-- Administrateurs
INSERT INTO administrateur (id, nom, prenom, date_naissance, departement, poste, telephone) VALUES
  ('A001', 'Mansouri',  'Karim',   '1979-10-12', 'Administration', 'Directeur',    '0699999991'),
  ('A002', 'Rahmoun',   'Nadia',   '1986-03-25', 'Comptabilité',   'Responsable',  '0699999992'),
  ('A003', 'Slimani',   'Adam',    '1991-07-19', 'Accueil',        'Agent',        '0699999993');

-- Rendez-vous
INSERT INTO rendez_vous (id, patient_id, medecin_id, date_heure) VALUES
  ('RDV0001', 'P001', 'M001', '2026-05-20 09:30:00'),
  ('RDV0002', 'P002', 'M004', '2026-05-20 14:00:00'),
  ('RDV0003', 'P003', 'M002', '2026-05-21 10:15:00'),
  ('RDV0004', 'P004', 'M003', '2026-05-22 11:00:00'),
  ('RDV0005', 'P005', 'M001', '2026-05-23 08:45:00');

-- Utilisateurs (passwords hashed with SHA-256 + per-user salt)
INSERT INTO utilisateur (username, password_hash, salt, role, nom_complet, actif, patient_id) VALUES
  ('admin', '3WRTIpsmbPErOrhwwfibG30JbKBY2AeEvZRra+YWHKQ=', 'tpBCEQusefbABwrI', 'ADMIN', 'Administrateur principal', TRUE, NULL),
  ('saidi', '/HK0GZBVVBIpzWE0dF6PDPg3++6PCT8zl9S15JnfQOg=', 'Kt527Z0Il3up4/ad', 'MEDECIN', 'Dr. Rachid Saidi', TRUE, NULL),
  ('bouzid', 'hO4+RE8T2zZGrwvVKJC49Z4b3u5E6VpRPGXZg8aU9As=', 'skDmmPN+7e1puXIp', 'MEDECIN', 'Dr. Fatima Bouzid', TRUE, NULL),
  ('nadia', 'aMoYzRH0SgJATNDAHnCqbR0xJN1Jv5L2zr434wO+zoQ=', 'EdA1L3Vz1amWrvCq', 'ADMIN', 'Nadia Rahmoun', TRUE, NULL),
  ('nurse', 'YXoSilNeKMTK7txSohZUIz+x2K5WiaBDewB+6CEzUsM=', 'Z1HVsgw8ZD6EdR7I', 'INFIRMIER', 'Infirmier de garde', TRUE, NULL),
  ('sara', 'giVZyGZGWq3KeKODw0AWzGfw2Icgw6rI8MMclxE01G4=', 'oorTEWpfMNiqB/XW', 'PATIENT', 'Sara Bennani', TRUE, 'P001'),
  ('youssef', 'zKCgqI/8lxPdYpuyBJNbxAbxtfpSSSkE22hRPU1Xxow=', 'bE5NtvYGPXvNdKM+', 'PATIENT', 'Youssef El Amrani', TRUE, 'P002'),
  ('leila', 'uNmL93NqzGvhnthavQ6P1gd/eYRx/3/1nO54C3GIny8=', 'TCzi1TvIySWQqyDT', 'PATIENT', 'Leila Tazi', TRUE, 'P003');

-- Drop in reverse FK order
DROP TABLE IF EXISTS audit_log CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS rendez_vous CASCADE;
DROP TABLE IF EXISTS patient CASCADE;
DROP TABLE IF EXISTS medecin CASCADE;
DROP TABLE IF EXISTS administrateur CASCADE;
DROP TABLE IF EXISTS service_hospitalier CASCADE;

-- service_hospitalier
CREATE TABLE service_hospitalier (
    nom            VARCHAR(80)  PRIMARY KEY,
    type           VARCHAR(20)  NOT NULL
                   CHECK (type IN ('CLINIQUE', 'ADMINISTRATIF', 'TECHNIQUE')),
    capacite_lits  INTEGER      NOT NULL DEFAULT 0
                   CHECK (capacite_lits >= 0)
);

-- patient
CREATE TABLE patient (
    id              VARCHAR(10) PRIMARY KEY,         -- e.g. P001
    nom             VARCHAR(80)  NOT NULL,
    prenom          VARCHAR(80)  NOT NULL,
    date_naissance  DATE         NOT NULL,
    groupe_sanguin  VARCHAR(4)
                    CHECK (groupe_sanguin IS NULL OR groupe_sanguin IN
                          ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
    telephone       VARCHAR(30),
    service_nom     VARCHAR(80)
                    REFERENCES service_hospitalier(nom) ON DELETE SET NULL
);

CREATE INDEX idx_patient_service ON patient(service_nom);

-- medecin
CREATE TABLE medecin (
    id              VARCHAR(10) PRIMARY KEY,         -- e.g. M001
    nom             VARCHAR(80)  NOT NULL,
    prenom          VARCHAR(80)  NOT NULL,
    date_naissance  DATE         NOT NULL,
    specialite      VARCHAR(20)  NOT NULL
                    CHECK (specialite IN ('CARDIOLOGIE','NEUROLOGIE','PEDIATRIE',
                                          'CHIRURGIE','DERMATOLOGIE','PSYCHIATRIE',
                                          'RADIOLOGIE','URGENCES')),
    matricule       VARCHAR(40),
    telephone       VARCHAR(30),
    service_nom     VARCHAR(80)
                    REFERENCES service_hospitalier(nom) ON DELETE SET NULL
);

CREATE INDEX idx_medecin_service ON medecin(service_nom);

-- administrateur
CREATE TABLE administrateur (
    id              VARCHAR(10) PRIMARY KEY,         -- e.g. A001
    nom             VARCHAR(80)  NOT NULL,
    prenom          VARCHAR(80)  NOT NULL,
    date_naissance  DATE         NOT NULL,
    departement     VARCHAR(80),
    poste           VARCHAR(80),
    telephone       VARCHAR(30)
);

-- rendez_vous
CREATE TABLE rendez_vous (
    id            VARCHAR(20) PRIMARY KEY,           -- e.g. RDV0001
    patient_id    VARCHAR(10) NOT NULL
                  REFERENCES patient(id) ON DELETE CASCADE,
    medecin_id    VARCHAR(10) NOT NULL
                  REFERENCES medecin(id) ON DELETE CASCADE,
    date_heure    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_rdv_patient ON rendez_vous(patient_id);
CREATE INDEX idx_rdv_medecin ON rendez_vous(medecin_id);
CREATE INDEX idx_rdv_date    ON rendez_vous(date_heure);

-- utilisateur (authentication)
CREATE TABLE utilisateur (
    username           VARCHAR(40)  PRIMARY KEY,
    password_hash      VARCHAR(128) NOT NULL,
    salt               VARCHAR(32)  NOT NULL,
    role               VARCHAR(20)  NOT NULL
                       CHECK (role IN ('ADMIN', 'MEDECIN', 'INFIRMIER', 'PATIENT')),
    nom_complet        VARCHAR(120),
    actif              BOOLEAN      NOT NULL DEFAULT TRUE,
    derniere_connexion TIMESTAMP,
    patient_id         VARCHAR(10) REFERENCES patient(id) ON DELETE CASCADE,
    -- A PATIENT row must reference a patient
    CONSTRAINT chk_utilisateur_patient_link
      CHECK ( (role <> 'PATIENT') OR (patient_id IS NOT NULL) )
);
CREATE INDEX idx_utilisateur_role        ON utilisateur(role);
CREATE INDEX idx_utilisateur_patient_id  ON utilisateur(patient_id);

-- audit_log : trace de toutes les opérations sensibles
CREATE TABLE audit_log (
    id           BIGSERIAL    PRIMARY KEY,
    ts           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    username     VARCHAR(40),
    role         VARCHAR(20),
    action       VARCHAR(20)  NOT NULL
                 CHECK (action IN ('CREATE','UPDATE','DELETE','LOGIN','LOGOUT')),
    target_table VARCHAR(40),
    target_id    VARCHAR(40),
    details      VARCHAR(500)
);
CREATE INDEX idx_audit_ts       ON audit_log(ts DESC);
CREATE INDEX idx_audit_username ON audit_log(username);
CREATE INDEX idx_audit_table    ON audit_log(target_table);

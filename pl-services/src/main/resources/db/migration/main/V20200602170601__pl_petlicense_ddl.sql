CREATE TABLE eg_pl_PetLicense(
  id character varying(64),
  accountid character varying(64),
  tenantId character varying(64),
  licenseType character varying(64),
  licenseNumber character varying(64),
  applicationNumber character varying(64),
  oldLicenseNumber character varying(64),
  applicationDate bigint,
  issuedDate bigint,
  validFrom bigint,
  validTo bigint,
  action character varying(64),
  status character varying(64),
  createdBy character varying(64),
  lastModifiedBy character varying(64),
  createdTime bigint,
  lastModifiedTime bigint,
  CONSTRAINT uk_eg_pl_PetLicense UNIQUE (id)
);


CREATE TABLE eg_pl_PetLicenseDetail(
  id character varying(64),
  channel character varying(64),
  colorId character varying(64),
  categoryId character varying(64),
  subcategoryId character varying(64),
  additionalDetail JSONB,
  petlicenseId character varying(256),
  createdBy character varying(64),
  lastModifiedBy character varying(64),
  createdTime bigint,
  lastModifiedTime bigint,
  CONSTRAINT pk_eg_pl_PetLicenseDetail PRIMARY KEY (id),
  CONSTRAINT fk_eg_pl_PetLicenseDetail FOREIGN KEY (petlicenseId) REFERENCES eg_pl_PetLicense (id)
);


CREATE TABLE eg_pl_ApplicationDocument(
    id character varying(64),
    tenantId character varying(64),
    documentType character varying(64),
    filestoreid character varying(64),
    petLicenseDetailId character varying(64),
    active boolean,
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_pl_ApplicationDocument PRIMARY KEY (id),
    CONSTRAINT fk_eg_pl_ApplicationDocument FOREIGN KEY (petLicenseDetailId) REFERENCES eg_pl_PetLicenseDetail (id)
);


CREATE TABLE eg_pl_VerificationDocument(
    id character varying(64),
    tenantId character varying(64),
    documentType character varying(64),
    filestoreid character varying(64),
    petLicenseDetailId character varying(64),
    active boolean,
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_pl_VerificationDocument PRIMARY KEY (id),
    CONSTRAINT fk_eg_pl_VerificationDocument FOREIGN KEY (petLicenseDetailId) REFERENCES eg_pl_PetLicenseDetail (id)
);


CREATE TABLE eg_pl_address(
    id character varying(64),
    tenantId character varying(64),
    doorNo character varying(64),
    latitude FLOAT,
    longitude FLOAT,
    buildingName character varying(64),
    addressId character varying(64),
    addressNumber character varying(64),
    type character varying(64),
    addressLine1 TYPE text,
    addressLine2 TYPE text,
    landmark character varying(64),
    street character varying(64),
    city character varying(64),
    locality character varying(64),
    pincode character varying(64),
    detail character varying(64),
    tradeLicenseDetailId character varying(64),
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_pl_address PRIMARY KEY (id),
    CONSTRAINT fk_eg_pl_address FOREIGN KEY (petLicenseDetailId) REFERENCES eg_pl_PetLicenseDetail (id)
      ON UPDATE CASCADE
      ON DELETE CASCADE
);


CREATE TABLE eg_pl_owner(
  id character varying(64),
  tenantId character varying(256),
  petLicenseDetailId character varying(64),
  isactive boolean,
  ownertype character varying(64),
  active boolean,
  createdby character varying(64),
  createdtime bigint,
  lastmodifiedby character varying(64),
  lastmodifiedtime bigint,
  CONSTRAINT pk_eg_pl_owner PRIMARY KEY (id,petLicenseDetailId),
  CONSTRAINT fk_eg_pl_owner FOREIGN KEY (petLicenseDetailId) REFERENCES eg_pl_PetLicenseDetail (id)
  ON UPDATE CASCADE
  ON DELETE CASCADE
);


CREATE TABLE eg_pl_document_owner (
  tenantId character varying(256),
  id character varying(64),
  petLicenseDetailId character varying(64),
  userid character varying(128),
  documenttype character varying(64),
  filestoreId character varying(64),
  active boolean,
  documentuid  character varying(64),
  createdby character varying(64),
  createdtime bigint,
  lastmodifiedby character varying(64),
  lastmodifiedtime bigint,
  CONSTRAINT uk_eg_pl_document_owner PRIMARY KEY (id),
  CONSTRAINT pk_eg_pl_document_owner UNIQUE (userid, petLicenseDetailId),
  CONSTRAINT fk_eg_pl_document_owner FOREIGN KEY (userid, petLicenseDetailId) REFERENCES eg_pl_owner (id, petLicenseDetailId)
  ON UPDATE CASCADE
  ON DELETE CASCADE
);


CREATE TABLE eg_pl_PetLicense_audit(
  id character varying(64),
  accountid character varying(64),
  tenantId character varying(64),
  licenseType character varying(64),
  licenseNumber character varying(64),
  applicationNumber character varying(64),
  oldLicenseNumber character varying(64),
  applicationDate bigint,
  issuedDate bigint,
  validFrom bigint,
  validTo bigint,
  action character varying(64),
  status character varying(64),
  createdBy character varying(64),
  lastModifiedBy character varying(64),
  createdTime bigint,
  lastModifiedTime bigint,
  tradeName character varying(256)
);
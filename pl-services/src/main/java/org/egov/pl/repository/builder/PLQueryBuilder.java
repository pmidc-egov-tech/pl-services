package org.egov.pl.repository.builder;

import java.util.List;

import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.PetLicenseSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PLQueryBuilder {

	private PLConfiguration config;

	@Autowired
	public PLQueryBuilder(PLConfiguration config) {
		this.config = config;
	}

	private static final String INNER_JOIN_STRING = " INNER JOIN ";
	private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";

	@Value("${egov.receipt.businessserviceTL}")
	private String businessServiceTL;

	@Value("${egov.receipt.businessserviceBPA}")
	private String businessServiceBPA;

	private static final String QUERY = "SELECT pl.*,tld.*,tlunit.*,tlacc.*,tlowner.*,"
			+ "tladdress.*,tlapldoc.*,tlverdoc.*,tlownerdoc.*,tlinsti.*,pl.id as tl_id,pl.tenantid as tl_tenantId,pl.lastModifiedTime as "
			+ "tl_lastModifiedTime,pl.createdBy as tl_createdBy,pl.lastModifiedBy as tl_lastModifiedBy,pl.createdTime as "
			+ "tl_createdTime,tld.id as tld_id,tladdress.id as tl_ad_id,tld.createdBy as tld_createdBy,"
			+ "tlowner.id as tlowner_uuid,tlowner.active as useractive,"
			+ "tld.createdTime as tld_createdTime,tld.lastModifiedBy as tld_lastModifiedBy,tld.createdTime as "
			+ "tld_createdTime,tlunit.id as tl_un_id,tlunit.tradeType as tl_un_tradeType,tlunit.uom as tl_un_uom,tlunit.active as tl_un_active,"
			+ "tlunit.uomvalue as tl_un_uomvalue,tlacc.id as tl_acc_id,tlacc.uom as tl_acc_uom,tlacc.uomvalue as tl_acc_uomvalue,tlacc.active as tl_acc_active,"
			+ "tlapldoc.id as tl_ap_doc_id,tlapldoc.documenttype as tl_ap_doc_documenttype,tlapldoc.filestoreid as tl_ap_doc_filestoreid,tlapldoc.active as tl_ap_doc_active,"
			+ "tlverdoc.id as tl_ver_doc_id,tlverdoc.documenttype as tl_ver_doc_documenttype,tlverdoc.filestoreid as tl_ver_doc_filestoreid,tlverdoc.active as tl_ver_doc_active,"
			+ "tlownerdoc.userid as docuserid,tlownerdoc.tradeLicenseDetailId as doctradelicensedetailid,tlownerdoc.id as ownerdocid,"
			+ "tlownerdoc.documenttype as ownerdocType,tlownerdoc.filestoreid as ownerfileStoreId,tlownerdoc.documentuid as ownerdocuid,tlownerdoc.active as ownerdocactive,"
			+ " tlinsti.id as instiid,tlinsti.name as authorisedpersonname,tlinsti.type as institutiontype,tlinsti.tenantid as institenantId,tlinsti.active as instiactive, "
			+ " tlinsti.instituionname as instiinstituionname, tlinsti.contactno as insticontactno, tlinsti.organisationregistrationno as instiorganisationregistrationno, tlinsti.address as instiaddress FROM eg_tl_tradelicense pl"
			+ INNER_JOIN_STRING + "eg_tl_tradelicensedetail tld ON tld.tradelicenseid = pl.id" + INNER_JOIN_STRING
			+ "eg_tl_address tladdress ON tladdress.tradelicensedetailid = tld.id" + INNER_JOIN_STRING
			+ "eg_tl_owner tlowner ON tlowner.tradelicensedetailid = tld.id" + INNER_JOIN_STRING
			+ "eg_tl_tradeunit tlunit ON tlunit.tradelicensedetailid = tld.id" + LEFT_OUTER_JOIN_STRING
			+ "eg_tl_accessory tlacc ON tlacc.tradelicensedetailid = tld.id" + LEFT_OUTER_JOIN_STRING
			+ "eg_tl_document_owner tlownerdoc ON tlownerdoc.userid = tlowner.id" + LEFT_OUTER_JOIN_STRING
			+ "eg_tl_applicationdocument tlapldoc ON tlapldoc.tradelicensedetailid = tld.id" + LEFT_OUTER_JOIN_STRING
			+ "eg_tl_verificationdocument tlverdoc ON tlverdoc.tradelicensedetailid = tld.id" + LEFT_OUTER_JOIN_STRING
			+ "eg_tl_institution tlinsti ON tlinsti.tradelicensedetailid = tld.id ";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY pl_id) offset_ FROM " + "({})" + " result) result_offset "
			+ "WHERE offset_ > ? AND offset_ <= ?";

	public String getTLSearchQuery(PetLicenseSearchCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		addBusinessServiceClause(criteria, preparedStmtList, builder);

		if (criteria.getAccountId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" pl.accountid = ? ");
			preparedStmtList.add(criteria.getAccountId());

			List<String> ownerIds = criteria.getOwnerIds();
			if (!CollectionUtils.isEmpty(ownerIds)) {
				builder.append(" OR (plowner.id IN (").append(createQuery(ownerIds)).append(")");
				addToPreparedStatement(preparedStmtList, ownerIds);
				addBusinessServiceClause(criteria, preparedStmtList, builder);
				builder.append(" AND plowner.active = ? )");
				preparedStmtList.add(true);
			}

			return builder.toString();
		}

		if (criteria.getTenantId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" pl.tenantid=? ");
			preparedStmtList.add(criteria.getTenantId());
		}
		List<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" pl.id IN (").append(createQuery(ids)).append(")");
			addToPreparedStatement(preparedStmtList, ids);
		}

		List<String> ownerIds = criteria.getOwnerIds();
		if (!CollectionUtils.isEmpty(ownerIds)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" (plowner.id IN (").append(createQuery(ownerIds)).append(")");
			addToPreparedStatement(preparedStmtList, ownerIds);
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" plowner.active = ? ) ");
			preparedStmtList.add(true);
		}

		if (criteria.getApplicationNumber() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("  pl.applicationnumber = ? ");
			preparedStmtList.add(criteria.getApplicationNumber());
		}

		if (criteria.getStatus() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("  pl.status = ? ");
			preparedStmtList.add(criteria.getStatus());
		}

		if (criteria.getLicenseNumber() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("  pl.licensenumber = ? ");
			preparedStmtList.add(criteria.getLicenseNumber());
		}

		if (criteria.getOldLicenseNumber() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("  pl.oldlicensenumber = ? ");
			preparedStmtList.add(criteria.getOldLicenseNumber());
		}

		if (criteria.getFromDate() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("  pl.applicationDate >= ? ");
			preparedStmtList.add(criteria.getFromDate());
		}

		if (criteria.getToDate() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("  pl.applicationDate <= ? ");
			preparedStmtList.add(criteria.getToDate());
		}

		// enrichCriteriaForUpdateSearch(builder,preparedStmtList,criteria);

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}

	private void addBusinessServiceClause(PetLicenseSearchCriteria criteria, List<Object> preparedStmtList,
			StringBuilder builder) {
		if ((criteria.getBusinessService() == null) || (businessServiceTL.equals(criteria.getBusinessService()))) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" (pl.businessservice=? or pl.businessservice isnull) ");
			preparedStmtList.add(businessServiceTL);
		} else if (businessServiceBPA.equals(criteria.getBusinessService())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" pl.businessservice=? ");
			preparedStmtList.add(businessServiceBPA);
		}
	}

	private String createQuery(List<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});
	}

	private String addPaginationWrapper(String query, List<Object> preparedStmtList,
			PetLicenseSearchCriteria criteria) {
		int limit = config.getDefaultLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
			limit = config.getMaxSearchLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);

		return finalQuery;
	}

	private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

}

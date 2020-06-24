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

	@Value("${egov.receipt.businessservicePL}")
	private String businessServicePL;

	@Value("${egov.receipt.businessserviceBPA}")
	private String businessServiceBPA;

	private static final String QUERY = "SELECT pl.*,pld.*,plowner.*,"
			+ "pladdress.*,plapldoc.*,plverdoc.*,plownerdoc.*,plinsti.*,pl.id as pl_id,pl.tenantid as pl_tenantId,pl.lastModifiedTime as "
			+ "pl_lastModifiedTime,pl.createdBy as pl_createdBy,pl.lastModifiedBy as pl_lastModifiedBy,pl.createdTime as "
			+ "pl_createdTime,pld.id as pld_id,pladdress.id as pl_ad_id,pld.createdBy as pld_createdBy,"
			+ "plowner.id as plowner_uuid,plowner.active as useractive,"
			+ "pld.createdTime as pld_createdTime,pld.lastModifiedBy as pld_lastModifiedBy,pld.createdTime as "
			+ "pld_createdTime,"
			+ "plapldoc.id as pl_ap_doc_id,plapldoc.documenttype as pl_ap_doc_documenttype,plapldoc.filestoreid as pl_ap_doc_filestoreid,plapldoc.active as pl_ap_doc_active,"
			+ "plverdoc.id as pl_ver_doc_id,plverdoc.documenttype as pl_ver_doc_documenttype,plverdoc.filestoreid as pl_ver_doc_filestoreid,plverdoc.active as pl_ver_doc_active,"
			+ "plownerdoc.userid as docuserid,plownerdoc.petLicenseDetailId as docpetlicensedetailid,plownerdoc.id as ownerdocid,"
			+ "plownerdoc.documenttype as ownerdocType,plownerdoc.filestoreid as ownerfileStoreId,plownerdoc.documentuid as ownerdocuid,plownerdoc.active as ownerdocactive,"
			+ INNER_JOIN_STRING
			+ "eg_pl_address pladdress ON pladdress.petlicensedetailid = pld.id" + INNER_JOIN_STRING
			+ "eg_pl_owner plowner ON plowner.petlicensedetailid = pld.id" + INNER_JOIN_STRING
			+ "eg_pl_document_owner plownerdoc ON plownerdoc.userid = plowner.id" + LEFT_OUTER_JOIN_STRING
			+ "eg_pl_applicationdocument plapldoc ON plapldoc.petlicensedetailid = pld.id" + LEFT_OUTER_JOIN_STRING
			+ "eg_pl_verificationdocument plverdoc ON plverdoc.petlicensedetailid = pld.id";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY pl_id) offset_ FROM " + "({})" + " result) result_offset "
			+ "WHERE offset_ > ? AND offset_ <= ?";

	public String getPLSearchQuery(PetLicenseSearchCriteria criteria, List<Object> preparedStmtList) {

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
		if ((criteria.getBusinessService() == null) || (businessServicePL.equals(criteria.getBusinessService()))) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" (pl.businessservice=? or pl.businessservice isnull) ");
			preparedStmtList.add(businessServicePL);
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

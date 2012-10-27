package com.redpill_linpro.alfresco.sales_demo.model;
import org.alfresco.service.namespace.QName;


public interface RpsdModel {
	
	public static final String RPSD_URI = "http://www.redpill-linpro.com/model/salesdemo/1.0";
	public static final String RPSD_SHORT = "rpsd";
	
	public static final QName TYPE_RPSD_DOCUMENT = QName.createQName(RPSD_URI, "document");
	public static final QName TYPE_RPSD_FOLDER = QName.createQName(RPSD_URI, "folder");
	
	public static final QName ASPECT_RPSD_BASE = QName.createQName(RPSD_URI, "baseAspect");
	
	public static final QName ASPECT_RPSD_DIRECT_REPORT = QName.createQName(RPSD_URI, "directReportAspect");	
	public static final QName PROP_RPSD_NAME = QName.createQName(RPSD_URI, "name");
	public static final QName PROP_RPSD_CONTENT = QName.createQName(RPSD_URI, "content");
	
	public static final QName ASPECT_RPSD_FIELD_REPORT = QName.createQName(RPSD_URI, "fieldReport");
	public static final QName PROP_RPSD_REPORT_DATE = QName.createQName(RPSD_URI, "reportDate");
	public static final QName PROP_RPSD_REPORT_LOCATION = QName.createQName(RPSD_URI, "reportLocation");
	public static final QName PROP_RPSD_REPORT_MEASUREMENT = QName.createQName(RPSD_URI, "reportMeasurement");
	public static final QName PROP_RPSD_REPORT_MEASUREMENT_VALUE = QName.createQName(RPSD_URI, "reportMeasurementValue");
	public static final QName PROP_RPSD_REPORT_RESPONSIBLE = QName.createQName(RPSD_URI, "reportResponsible");
	public static final QName PROP_RPSD_REPORT_CONTACT = QName.createQName(RPSD_URI, "reportContact");
}

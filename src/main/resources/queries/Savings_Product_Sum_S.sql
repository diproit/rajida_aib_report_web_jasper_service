SELECT 
    pl_month_tb.id,
    pl_month_tb.pl_account_id,
    pl_month_tb.branch_month_id,
    pl_month_tb.closing_balance,
    pl_month_tb.status,
    pl_account.id AS COLUMN_6,
    ci_customer.id AS COLUMN_7,
    pl_account.ci_customer_id,
    ci_customer.customer_number,
    pl_account_type.id AS COLUMN_10,
    pl_month_tb.id AS COLUMN_11,
    gl_branch.telephone,
    pl_month_tb.status AS COLUMN_13,
    it_institute.id AS COLUMN_14,
    pl_account_type.it_institute_id,
    pl_account.ref_account_number,
    it_user_master.id AS COLUMN_17,
    it_user_master.name,
    pl_account_type.it_institute_id AS COLUMN_19,
    it_generated_reports.id AS COLUMN_20,
    it_generated_reports.user_id,
    it_generated_reports.it_institute_id AS COLUMN_22,
    it_generated_reports.filter_1_text,
    it_generated_reports.filter_2_text,
    it_generated_reports.filter_1_value,
    it_generated_reports.filter_2_value,
    pl_account_type.pl_account_category_id,
    pl_account.status AS COLUMN_28,
    gl_branch.id AS COLUMN_29,
    gl_branch.it_institute_id AS COLUMN_30,
    gl_branch.status AS COLUMN_31,
    it_institute.name_ln1,
    it_institute.name_ln2,
    it_institute.name_ln3,
    it_institute.address_ln1,
    it_institute.address_ln3,
    it_institute.address_ln2,
    ci_customer.full_name_ln1,
    ci_customer.full_name_ln2,
    ci_customer.full_name_ln3,
    pl_account_type.name_ln1 AS COLUMN_41,
    pl_account_type.name_ln2 AS COLUMN_42,
    pl_account_type.name_ln3 AS COLUMN_43,
    gl_branch.name_ln1 AS COLUMN_44,
    gl_branch.name_ln2 AS COLUMN_45,
    gl_branch.name_ln3 AS COLUMN_46,
    it_branch_month.id AS COLUMN_47,
    it_branch_month.status AS COLUMN_48,
    it_generated_reports.branch_id
FROM 
    pl_month_tb
INNER JOIN pl_account ON 
    pl_month_tb.pl_account_id = pl_account.id
INNER JOIN ci_customer ON 
    pl_account.ci_customer_id = ci_customer.id
INNER JOIN pl_account_type ON 
    pl_account.pl_account_type_id = pl_account_type.id
INNER JOIN gl_branch ON 
    pl_account.branch_id = gl_branch.id
INNER JOIN it_institute ON 
    it_institute.id = gl_branch.it_institute_id
INNER JOIN it_branch_month ON 
    it_branch_month.id = pl_month_tb.branch_month_id
INNER JOIN it_generated_reports ON 
    it_generated_reports.branch_id = gl_branch.id
INNER JOIN it_user_master ON 
    it_user_master.id = it_generated_reports.user_id
WHERE
    it_generated_reports.id = :parm_a
ORDER BY 
    pl_account_type.id ASC,
    pl_account.ref_account_number ASC
Limit 10
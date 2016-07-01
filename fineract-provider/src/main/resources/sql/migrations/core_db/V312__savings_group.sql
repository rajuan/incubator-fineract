
ALTER TABLE `m_group` 
ADD COLUMN `group_type_enum` SMALLINT NOT NULL DEFAULT 1 AFTER `status_enum`;

ALTER TABLE `m_share_product` 
ADD COLUMN `savings_group_id` BIGINT(20) NULL,
ADD CONSTRAINT `m_share_product_savings_groupid`
    FOREIGN KEY (`savings_group_id`)
    REFERENCES `m_group` (`id`);

CREATE TABLE IF NOT EXISTS `m_savings_group_cycle` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT(20) NOT NULL,
  `cycle_num` BIGINT(20) NOT NULL,
  `status_enum` SMALLINT(4) NOT NULL,
  `currency_code` varchar(3) NOT NULL,
  `currency_digits` smallint(5) NOT NULL,
  `currency_multiplesof` smallint(5) DEFAULT NULL,
  `expected_start_date` DATE NULL,
  `actual_start_date` DATE NULL,
  `expected_end_date` DATE NULL,
  `actual_end_date` DATE NULL,
  `expected_num_of_meetings` INT(8) NULL,
  `num_of_meetings_completed` INT(8) NULL,
  `num_of_meetings_pending` INT(8) NULL,
  `is_share_based` TINYINT(1) NOT NULL,
  `unit_price_of_share` DECIMAL NOT NULL DEFAULT 1,
  `share_product_id` BIGINT(20) NULL,
  `is_client_additions_allowed_in_active_cycle` TINYINT(1) NOT NULL,
  `is_client_exit_allowed_in_active_cycle` TINYINT(1) NOT NULL,
  `does_individual_client_exit_forfeit_gains` TINYINT(1) NOT NULL,
  `deposits_payment_strategy` VARCHAR(5) NOT NULL,
  PRIMARY KEY (`id`) ,
  INDEX `fk_m_savings_group_params_groupid_idx` (`group_id` ASC) ,
  INDEX `fk_m_savings_group_cycle_share_product_idx` (`share_product_id` ASC) ,
  CONSTRAINT `fk_m_savings_group_cycle_groupid`
    FOREIGN KEY (`group_id`)
    REFERENCES `m_group` (`id`),
  CONSTRAINT `fk_m_savings_group_cycle_share_product`
    FOREIGN KEY (`share_product_id`)
    REFERENCES `m_share_product` (`id`));

CREATE TABLE IF NOT EXISTS `m_savings_group_funds` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `group_id` BIGINT(20) NOT NULL,
  `minimum_deposit_per_meeting` DECIMAL NOT NULL,
  `maximum_deposit_per_meeting` DECIMAL NOT NULL,
  `loan_product_id` BIGINT(20) NULL,
  `cycle_id` BIGINT(20) NOT NULL,
  `fund_status` TINYINT(1) NOT NULL,
  `total_cash_in_hand` DECIMAL NULL,
  `total_cash_in_bank` DECIMAL NULL,
  `total_deposits` DECIMAL NULL,
  `total_loan_portfolio` DECIMAL NULL,
  `total_fee_collected` DECIMAL NULL,
  `total_expenses` DECIMAL NULL,
  `total_income` DECIMAL NULL,
  `is_loan_limit_based_on_savings` TINYINT(1) NOT NULL,
  `loan_limit_amount` DECIMAL NULL,
  `loan_limit_factor` SMALLINT NULL,
  PRIMARY KEY (`id`) ,
  INDEX `fk_m_savings_group_params_groupid_idx` (`group_id` ASC) ,
  INDEX `fk_m_savings_group_funds_loanproduct_idx` (`loan_product_id` ASC) ,
  CONSTRAINT `fk_m_savings_group_funds_groupid`
    FOREIGN KEY (`group_id`)
    REFERENCES `m_group` (`id`),
  CONSTRAINT `fk_m_savings_group_funds_loanproduct`
    FOREIGN KEY (`loan_product_id`)
    REFERENCES `m_product_loan` (`id`),
  CONSTRAINT `fk_m_savings_group_funds_cycleid`
    FOREIGN KEY (`cycle_id`)
    REFERENCES `m_savings_group_cycle` (`id`));


ALTER TABLE `m_product_loan` 
ADD COLUMN `savings_group_fund_id` BIGINT(20) NULL,
ADD CONSTRAINT `FK_m_product_loan_fundid`
    FOREIGN KEY (`savings_group_fund_id`)
    REFERENCES `m_savings_group_funds` (`id`);


CREATE TABLE IF NOT EXISTS `m_savings_group_transactions_schedule` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT(20) NOT NULL,
  `cycle_id` BIGINT(20) NOT NULL,
  `fund_id` BIGINT(20) NOT NULL,
  `client_id` BIGINT(20) NOT NULL,
  `transaction_type_enum` SMALLINT NOT NULL,
  `transaction_date` DATE NOT NULL,
  `transaction_amount` DECIMAL NOT NULL,
  `is_waived` TINYINT(1) NULL,
  `is_paid` TINYINT(1) NULL,
  PRIMARY KEY (`id`) ,
  INDEX `fk_m_savings_group_params_groupid_idx` (`group_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_groupid_idx` (`cycle_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_fundid_idx` (`fund_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_clientid_idx` (`client_id` ASC) ,
  CONSTRAINT `fk_m_savings_group_transactions_schedule_groupid`
    FOREIGN KEY (`group_id`)
    REFERENCES `m_group` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_schedule_cycleid`
    FOREIGN KEY (`cycle_id`)
    REFERENCES `m_savings_group_cycle` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_schedule_fundid`
    FOREIGN KEY (`fund_id`)
    REFERENCES `m_savings_group_funds` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_schedule_clientid`
    FOREIGN KEY (`client_id`)
    REFERENCES `m_client` (`id`));


CREATE TABLE IF NOT EXISTS `m_savings_group_transactions` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT(20) NOT NULL,
  `cycle_id` BIGINT(20) NOT NULL,
  `fund_id` BIGINT(20) NOT NULL,
  `client_id` BIGINT(20) NULL,
  `schedule_id` BIGINT(20) NULL,
  `transaction_type_enum` SMALLINT NOT NULL,
  `transaction_date` DATE NOT NULL,
  `transaction_amount` DECIMAL NOT NULL,
  `is_reversed` TINYINT(1) NULL,
  `is_overpaid` TINYINT(1) NULL,
  `is_underpaid` TINYINT(1) NULL,
  `overpaid_amount` DECIMAL NULL,
  `underpaid_amount` DECIMAL NULL,
  `share_transaction_id` BIGINT(20) NULL,
  PRIMARY KEY (`id`) ,
  INDEX `fk_m_savings_group_params_groupid_idx` (`group_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_groupid_idx` (`cycle_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_fundid_idx` (`fund_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_clientid_idx` (`client_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_scheduleid_idx` (`schedule_id` ASC) ,
  INDEX `fk_m_savings_group_transactions_sharetransactionid_idx` (`share_transaction_id` ASC) ,
  CONSTRAINT `fk_m_savings_group_transactions_groupid`
    FOREIGN KEY (`group_id`)
    REFERENCES `m_group` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_cycleid`
    FOREIGN KEY (`cycle_id`)
    REFERENCES `m_savings_group_cycle` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_fundid`
    FOREIGN KEY (`fund_id`)
    REFERENCES `m_savings_group_funds` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_clientid`
    FOREIGN KEY (`client_id`)
    REFERENCES `m_client` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_scheduleid`
    FOREIGN KEY (`schedule_id`)
    REFERENCES `m_savings_group_transactions_schedule` (`id`),
  CONSTRAINT `fk_m_savings_group_transactions_sharetransactionid`
    FOREIGN KEY (`share_transaction_id`)
    REFERENCES `m_share_account_transactions` (`id`));


CREATE TABLE IF NOT EXISTS `m_savings_group_charges` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `fund_id` BIGINT(20) NOT NULL,
  `charge_applies_to_enum` SMALLINT NULL,
  `charge_time_enum` SMALLINT NULL,
  `charge_calculation_enum` SMALLINT NULL,
  `amount` DECIMAL NULL,
  `is_penalty` TINYINT(1) NULL,
  `is_active` TINYINT(1) NULL,
  PRIMARY KEY (`id`) ,
  INDEX `fk_m_savings_group_charges_fundid_idx` (`fund_id` ASC) ,
  CONSTRAINT `fk_m_savings_group_charges_fundid`
    FOREIGN KEY (`fund_id`)
    REFERENCES `m_savings_group_funds` (`id`));


CREATE TABLE IF NOT EXISTS `m_savings_group_fund_loan_product_details` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `fund_id` BIGINT(20) NOT NULL,
  `annual_nominal_interest_rate` DECIMAL NOT NULL,
  `interest_method_enum` SMALLINT NOT NULL,
  `interest_calculated_in_period_enum` SMALLINT NOT NULL,
  `repay_every` SMALLINT NOT NULL,
  `repayment_period_frequency_enum` SMALLINT NOT NULL,
  `number_of_repayments` SMALLINT NOT NULL,
  `min_number_of_repayments` SMALLINT NULL,
  `max_number_of_repayments` SMALLINT NULL,
  `amortization_method_enum` SMALLINT NOT NULL,
  `loan_transaction_strategy_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`) ,
  INDEX `m_sgf_loanproductdet_fundid_idx` (`fund_id` ASC) ,
  INDEX `m_sgf_loanproductdet_loantransactionstrategy_idx` (`loan_transaction_strategy_id` ASC) ,
  CONSTRAINT `m_sgf_loanproductdet_fundid`
    FOREIGN KEY (`fund_id`)
    REFERENCES `m_savings_group_funds` (`id`),
  CONSTRAINT `m_sgf_loanproductdet_loantransactionstrategy`
    FOREIGN KEY (`loan_transaction_strategy_id`)
    REFERENCES `ref_loan_transaction_processing_strategy` (`id`));


CREATE TABLE IF NOT EXISTS `m_savings_group_client_share_account_mapping` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT(20) NOT NULL,
  `client_id` BIGINT(20) NOT NULL,
  `share_account_id` BIGINT(20) NOT NULL,
  `is_active` TINYINT(1) NOT NULL,
  PRIMARY KEY (`id`) ,
  INDEX `fk_sgc_to_sa_mapping_groupid_idx` (`group_id` ASC) ,
  INDEX `fk_sgc_to_sa_mapping_clientid_idx` (`client_id` ASC) ,
  INDEX `fk_sgc_to_sa_mapping_shareaccountid_idx` (`share_account_id` ASC) ,
  UNIQUE INDEX `fk_sgc_to_sa_mapping_unique_idx` (`group_id` ASC, `client_id` ASC, `share_account_id` ASC, `is_active` ASC) ,
  CONSTRAINT `fk_sgc_to_sa_mapping_groupid`
    FOREIGN KEY (`group_id`)
    REFERENCES `m_group` (`id`),
  CONSTRAINT `fk_sgc_to_sa_mapping_clientid`
    FOREIGN KEY (`client_id`)
    REFERENCES `m_client` (`id`),
  CONSTRAINT `fk_sgc_to_sa_mapping_shareaccountid`
    FOREIGN KEY (`share_account_id`)
    REFERENCES `m_share_account` (`id`));

INSERT INTO `m_permission`(`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES
('SGCYCLE', 'READ_SGCYCLE', 'SGCYCLE', 'READ', 0),
('SGCYCLE', 'CREATE_SGCYCLE', 'SGCYCLE', 'CREATE', 0),
('SGCYCLE', 'UPDATE_SGCYCLE', 'SGCYCLE', 'UPDATE', 0),
('SGCYCLE', 'ACTIVATE_SGCYCLE', 'SGCYCLE', 'UPDATE', 0),
('SGCYCLE', 'SHAREOUT_SGCYCLE', 'SGCYCLE', 'UPDATE', 0),
('SGCYCLE', 'SHAREOUTCLOSE_SGCYCLE', 'SGCYCLE', 'UPDATE', 0),
('SGFUND', 'READ_SGFUND', 'SGFUND', 'READ', 0),
('SGFUND', 'CREATE_SGFUND', 'SGFUND', 'CREATE', 0),
('SGFUND', 'UPDATE_SGFUND', 'SGFUND', 'UPDATE', 0),
('SGFUND', 'DELETE_SGFUND', 'SGFUND', 'DELETE', 0);

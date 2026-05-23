BEGIN;

  -- Delete only records created by this demo script so it can be re-run.
  DELETE FROM processing_job_status_history
  WHERE processing_job_id IN (
      '10000000-0000-0000-0000-000000000001',
      '10000000-0000-0000-0000-000000000002',
      '10000000-0000-0000-0000-000000000003',
      '10000000-0000-0000-0000-000000000004',
      '10000000-0000-0000-0000-000000000005',
      '10000000-0000-0000-0000-000000000006',
      '10000000-0000-0000-0000-000000000007'
  );

  DELETE FROM processing_jobs
  WHERE id IN (
      '10000000-0000-0000-0000-000000000001',
      '10000000-0000-0000-0000-000000000002',
      '10000000-0000-0000-0000-000000000003',
      '10000000-0000-0000-0000-000000000004',
      '10000000-0000-0000-0000-000000000005',
      '10000000-0000-0000-0000-000000000006',
      '10000000-0000-0000-0000-000000000007'
  );

  DELETE FROM uploaded_files
  WHERE id IN (
      '20000000-0000-0000-0000-000000000001',
      '20000000-0000-0000-0000-000000000002',
      '20000000-0000-0000-0000-000000000003',
      '20000000-0000-0000-0000-000000000004',
      '20000000-0000-0000-0000-000000000005',
      '20000000-0000-0000-0000-000000000006',
      '20000000-0000-0000-0000-000000000007'
  );

  INSERT INTO uploaded_files (
      id,
      original_filename,
      stored_filename,
      storage_path,
      content_type,
      file_size,
      checksum,
      status,
      uploaded_by_user_id,
      uploaded_at,
      updated_at
  ) VALUES
  (
      '20000000-0000-0000-0000-000000000001',
      'sales-january-2026.xlsx',
      'sales-january-2026.xlsx',
      'analyst01/sales-january-2026.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      125800,
      repeat('1', 64),
      'MISSING',
      '00000000-0000-0000-0000-000000000001',
      CURRENT_TIMESTAMP - INTERVAL '7 days',
      NULL
  ),
  (
      '20000000-0000-0000-0000-000000000002',
      'sales-february-2026.xlsx',
      'sales-february-2026.xlsx',
      'analyst01/sales-february-2026.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      142300,
      repeat('2', 64),
      'MISSING',
      '00000000-0000-0000-0000-000000000001',
      CURRENT_TIMESTAMP - INTERVAL '6 days',
      CURRENT_TIMESTAMP - INTERVAL '5 days'
  ),
  (
      '20000000-0000-0000-0000-000000000003',
      'sales-march-2026.xlsx',
      'sales-march-2026.xlsx',
      'analyst01/sales-march-2026.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      131400,
      repeat('3', 64),
      'MISSING',
      '00000000-0000-0000-0000-000000000001',
      CURRENT_TIMESTAMP - INTERVAL '5 days',
      CURRENT_TIMESTAMP - INTERVAL '4 days'
  ),
  (
      '20000000-0000-0000-0000-000000000004',
      'sales-april-2026.xlsx',
      'sales-april-2026.xlsx',
      'analyst01/sales-april-2026.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      149200,
      repeat('4', 64),
      'MISSING',
      '00000000-0000-0000-0000-000000000001',
      CURRENT_TIMESTAMP - INTERVAL '4 days',
      CURRENT_TIMESTAMP - INTERVAL '3 days'
  ),
  (
      '20000000-0000-0000-0000-000000000005',
      'sales-may-2026.xlsx',
      'sales-may-2026.xlsx',
      'analyst01/sales-may-2026.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      156700,
      repeat('5', 64),
      'MISSING',
      '00000000-0000-0000-0000-000000000001',
      CURRENT_TIMESTAMP - INTERVAL '3 days',
      CURRENT_TIMESTAMP - INTERVAL '2 days'
  ),
  (
      '20000000-0000-0000-0000-000000000006',
      'customer-balances-q1-2026.xlsx',
      'customer-balances-q1-2026.xlsx',
      'analyst01/customer-balances-q1-2026.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      98000,
      repeat('6', 64),
      'MISSING',
      '00000000-0000-0000-0000-000000000001',
      CURRENT_TIMESTAMP - INTERVAL '2 days',
      CURRENT_TIMESTAMP - INTERVAL '1 day'
  ),
  (
      '20000000-0000-0000-0000-000000000007',
      'provider-exposure-may-2026.xlsx',
      'provider-exposure-may-2026.xlsx',
      'analyst01/provider-exposure-may-2026.xlsx',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      87300,
      repeat('7', 64),
      'MISSING',
      '00000000-0000-0000-0000-000000000001',
      CURRENT_TIMESTAMP - INTERVAL '1 day',
      CURRENT_TIMESTAMP
  );

  INSERT INTO processing_jobs (
      id,
      uploaded_file_id,
      status,
      message,
      triggered_by_user_id,
      triggered_at,
      processing_completed_at,
      failure_reason,
      approved_by_user_id,
      approved_at,
      rejected_by_user_id,
      rejected_at,
      rejection_reason,
      revoked_by_user_id,
      revoked_at,
      revocation_reason,
      created_at,
      updated_at
  ) VALUES
  (
      '10000000-0000-0000-0000-000000000001',
      '20000000-0000-0000-0000-000000000001',
      'PENDING_EXECUTION',
      'Waiting for administrator execution',
      NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '7 days',
      NULL
  ),
  (
      '10000000-0000-0000-0000-000000000002',
      '20000000-0000-0000-0000-000000000002',
      'PROCESSING',
      'Processing workbook rows',
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '5 days',
      NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '6 days',
      CURRENT_TIMESTAMP - INTERVAL '5 days'
  ),
  (
      '10000000-0000-0000-0000-000000000003',
      '20000000-0000-0000-0000-000000000003',
      'PROCESSING_FAILED',
      'Processing failed',
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '4 days 2 hours',
      CURRENT_TIMESTAMP - INTERVAL '4 days',
      'Required column tax_identifier was not found',
      NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '5 days',
      CURRENT_TIMESTAMP - INTERVAL '4 days'
  ),
  (
      '10000000-0000-0000-0000-000000000004',
      '20000000-0000-0000-0000-000000000004',
      'AWAITING_APPROVAL',
      'Processing completed; awaiting administrator approval',
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '3 days 2 hours',
      CURRENT_TIMESTAMP - INTERVAL '3 days',
      NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '4 days',
      CURRENT_TIMESTAMP - INTERVAL '3 days'
  ),
  (
      '10000000-0000-0000-0000-000000000005',
      '20000000-0000-0000-0000-000000000005',
      'APPROVED',
      'Submission approved',
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '2 days 4 hours',
      CURRENT_TIMESTAMP - INTERVAL '2 days 3 hours',
      NULL,
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '2 days',
      NULL, NULL, NULL, NULL, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '3 days',
      CURRENT_TIMESTAMP - INTERVAL '2 days'
  ),
  (
      '10000000-0000-0000-0000-000000000006',
      '20000000-0000-0000-0000-000000000006',
      'REJECTED',
      'Submission rejected',
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours',
      CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours',
      NULL, NULL, NULL,
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '1 day',
      'Totals do not match supporting documentation',
      NULL, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '2 days',
      CURRENT_TIMESTAMP - INTERVAL '1 day'
  ),
  (
      '10000000-0000-0000-0000-000000000007',
      '20000000-0000-0000-0000-000000000007',
      'REVOKED',
      'Previously approved submission revoked',
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '20 hours',
      CURRENT_TIMESTAMP - INTERVAL '19 hours',
      NULL,
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '18 hours',
      NULL, NULL, NULL,
      '00000000-0000-0000-0000-000000000002',
      CURRENT_TIMESTAMP - INTERVAL '2 hours',
      'New evidence invalidated the approval',
      CURRENT_TIMESTAMP - INTERVAL '1 day',
      CURRENT_TIMESTAMP - INTERVAL '2 hours'
  );

  -- History: pending job.
  INSERT INTO processing_job_status_history (
      id, processing_job_id, previous_status, new_status,
      transition_source, transitioned_by_user_id, reason, created_at
  ) VALUES
  (
      '30000000-0000-0000-0000-000000000001',
      '10000000-0000-0000-0000-000000000001',
      NULL, 'PENDING_EXECUTION',
      'USER', '00000000-0000-0000-0000-000000000001',
      'File uploaded and queued for execution',
      CURRENT_TIMESTAMP - INTERVAL '7 days'
  );

  -- History: currently processing.
  INSERT INTO processing_job_status_history (
      id, processing_job_id, previous_status, new_status,
      transition_source, transitioned_by_user_id, reason, created_at
  ) VALUES
  (
      '30000000-0000-0000-0000-000000000002',
      '10000000-0000-0000-0000-000000000002',
      NULL, 'PENDING_EXECUTION',
      'USER', '00000000-0000-0000-0000-000000000001',
      'File uploaded and queued for execution',
      CURRENT_TIMESTAMP - INTERVAL '6 days'
  ),
  (
      '30000000-0000-0000-0000-000000000003',
      '10000000-0000-0000-0000-000000000002',
      'PENDING_EXECUTION', 'PROCESSING',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator started processing',
      CURRENT_TIMESTAMP - INTERVAL '5 days'
  );

  -- History: failed processing.
  INSERT INTO processing_job_status_history (
      id, processing_job_id, previous_status, new_status,
      transition_source, transitioned_by_user_id, reason, created_at
  ) VALUES
  (
      '30000000-0000-0000-0000-000000000004',
      '10000000-0000-0000-0000-000000000003',
      NULL, 'PENDING_EXECUTION',
      'USER', '00000000-0000-0000-0000-000000000001',
      'File uploaded and queued for execution',
      CURRENT_TIMESTAMP - INTERVAL '5 days'
  ),
  (
      '30000000-0000-0000-0000-000000000005',
      '10000000-0000-0000-0000-000000000003',
      'PENDING_EXECUTION', 'PROCESSING',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator started processing',
      CURRENT_TIMESTAMP - INTERVAL '4 days 2 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000006',
      '10000000-0000-0000-0000-000000000003',
      'PROCESSING', 'PROCESSING_FAILED',
      'SYSTEM', NULL,
      'Required column tax_identifier was not found',
      CURRENT_TIMESTAMP - INTERVAL '4 days'
  );

  -- History: awaiting approval.
  INSERT INTO processing_job_status_history (
      id, processing_job_id, previous_status, new_status,
      transition_source, transitioned_by_user_id, reason, created_at
  ) VALUES
  (
      '30000000-0000-0000-0000-000000000007',
      '10000000-0000-0000-0000-000000000004',
      NULL, 'PENDING_EXECUTION',
      'USER', '00000000-0000-0000-0000-000000000001',
      'File uploaded and queued for execution',
      CURRENT_TIMESTAMP - INTERVAL '4 days'
  ),
  (
      '30000000-0000-0000-0000-000000000008',
      '10000000-0000-0000-0000-000000000004',
      'PENDING_EXECUTION', 'PROCESSING',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator started processing',
      CURRENT_TIMESTAMP - INTERVAL '3 days 2 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000009',
      '10000000-0000-0000-0000-000000000004',
      'PROCESSING', 'AWAITING_APPROVAL',
      'SYSTEM', NULL,
      'Automatic processing completed successfully',
      CURRENT_TIMESTAMP - INTERVAL '3 days'
  );

  -- History: approved.
  INSERT INTO processing_job_status_history (
      id, processing_job_id, previous_status, new_status,
      transition_source, transitioned_by_user_id, reason, created_at
  ) VALUES
  (
      '30000000-0000-0000-0000-000000000010',
      '10000000-0000-0000-0000-000000000005',
      NULL, 'PENDING_EXECUTION',
      'USER', '00000000-0000-0000-0000-000000000001',
      'File uploaded and queued for execution',
      CURRENT_TIMESTAMP - INTERVAL '3 days'
  ),
  (
      '30000000-0000-0000-0000-000000000011',
      '10000000-0000-0000-0000-000000000005',
      'PENDING_EXECUTION', 'PROCESSING',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator started processing',
      CURRENT_TIMESTAMP - INTERVAL '2 days 4 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000012',
      '10000000-0000-0000-0000-000000000005',
      'PROCESSING', 'AWAITING_APPROVAL',
      'SYSTEM', NULL,
      'Automatic processing completed successfully',
      CURRENT_TIMESTAMP - INTERVAL '2 days 3 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000013',
      '10000000-0000-0000-0000-000000000005',
      'AWAITING_APPROVAL', 'APPROVED',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator approved submission',
      CURRENT_TIMESTAMP - INTERVAL '2 days'
  );

  -- History: rejected.
  INSERT INTO processing_job_status_history (
      id, processing_job_id, previous_status, new_status,
      transition_source, transitioned_by_user_id, reason, created_at
  ) VALUES
  (
      '30000000-0000-0000-0000-000000000014',
      '10000000-0000-0000-0000-000000000006',
      NULL, 'PENDING_EXECUTION',
      'USER', '00000000-0000-0000-0000-000000000001',
      'File uploaded and queued for execution',
      CURRENT_TIMESTAMP - INTERVAL '2 days'
  ),
  (
      '30000000-0000-0000-0000-000000000015',
      '10000000-0000-0000-0000-000000000006',
      'PENDING_EXECUTION', 'PROCESSING',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator started processing',
      CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000016',
      '10000000-0000-0000-0000-000000000006',
      'PROCESSING', 'AWAITING_APPROVAL',
      'SYSTEM', NULL,
      'Automatic processing completed successfully',
      CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000017',
      '10000000-0000-0000-0000-000000000006',
      'AWAITING_APPROVAL', 'REJECTED',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Totals do not match supporting documentation',
      CURRENT_TIMESTAMP - INTERVAL '1 day'
  );

  -- History: revoked.
  INSERT INTO processing_job_status_history (
      id, processing_job_id, previous_status, new_status,
      transition_source, transitioned_by_user_id, reason, created_at
  ) VALUES
  (
      '30000000-0000-0000-0000-000000000018',
      '10000000-0000-0000-0000-000000000007',
      NULL, 'PENDING_EXECUTION',
      'USER', '00000000-0000-0000-0000-000000000001',
      'File uploaded and queued for execution',
      CURRENT_TIMESTAMP - INTERVAL '1 day'
  ),
  (
      '30000000-0000-0000-0000-000000000019',
      '10000000-0000-0000-0000-000000000007',
      'PENDING_EXECUTION', 'PROCESSING',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator started processing',
      CURRENT_TIMESTAMP - INTERVAL '20 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000020',
      '10000000-0000-0000-0000-000000000007',
      'PROCESSING', 'AWAITING_APPROVAL',
      'SYSTEM', NULL,
      'Automatic processing completed successfully',
      CURRENT_TIMESTAMP - INTERVAL '19 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000021',
      '10000000-0000-0000-0000-000000000007',
      'AWAITING_APPROVAL', 'APPROVED',
      'USER', '00000000-0000-0000-0000-000000000002',
      'Administrator approved submission',
      CURRENT_TIMESTAMP - INTERVAL '18 hours'
  ),
  (
      '30000000-0000-0000-0000-000000000022',
      '10000000-0000-0000-0000-000000000007',
      'APPROVED', 'REVOKED',
      'USER', '00000000-0000-0000-0000-000000000002',
      'New evidence invalidated the approval',
      CURRENT_TIMESTAMP - INTERVAL '2 hours'
  );

  COMMIT;
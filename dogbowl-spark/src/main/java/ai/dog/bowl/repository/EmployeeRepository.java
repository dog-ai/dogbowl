/*
 * Copyright (C) 2016, Hugo Freire <hugo@dog.ai>. All rights reserved.
 */

package ai.dog.bowl.repository;

import java.util.List;

public interface EmployeeRepository {
  List<String> findEmployeesByCompanyId(String companyId);
}

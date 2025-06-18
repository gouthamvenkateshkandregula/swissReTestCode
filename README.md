# swissReTestCode
This Repo is created to write the test for swissRe code test and share back the github URL to the SwissRe team.

#################################################
Steps to followed:

Written 5 different classes for 6 questions asked:

1. Use the above table and create similar records [approximately:50 employees]
   and write the same into a excel file.
   Answer: Run the EmployeeDataGenerator
   Output: [employees.xlsx](employees.xlsx)

2. Use a library to fetch the data from excel file and do the following tasks.
3. Identify who all employees are eligible for gratuity [assuming gratuity eligibility is
   for employee who served more than 60 months] based on DOJ column (For 2 & 3 questions run the below class)
   Answer: Run the GratuityEligibilityChecker
   Output: [gratuity_eligible_employees.xlsx](gratuity_eligible_employees.xlsx)

4. Write a method to calculate the employee whose salary is greater than his
   manager's salary
   Answer: Run the EmployeeSalaryAnalyzer
   Output: [salary_analysis_results.xlsx](salary_analysis_results.xlsx)

5. Build employee hierarchy tree (Org. structure) based on manager_id [output
   should be written into a JSON file]
   Answer: Run the EmployeeHierarchyBuilder
   Output: [employee_hierarchy.json](employee_hierarchy.json)

6. Write an SQL to return the row of an employee whose salary is nth highest in
   descending order
   Answer: Run the NthSalaryFinder 
             (OR) 
        Here the direct SQL query for this question below:
    sql:
    SELECT * FROM (
    SELECT *,
    DENSE_RANK() OVER (ORDER BY salary DESC) as salary_rank
    FROM employees
    ) ranked_employees
    WHERE salary_rank = n;

*****DENSE_RANK(): If multiple employees have the same salary, they all get the same rank, and the next rank continues sequentially **********
#################################################


#################################################
Output's were also generated under the root folder while running the each main class
#################################################

(() => {
  const apiBaseUrl = window.APP_CONFIG?.apiBaseUrl;
  if (!apiBaseUrl) {
    throw new Error("The frontend API_BASE_URL configuration is missing.");
  }
  const api = apiBaseUrl.replace(/\/+$/, "");

  const elements = {
    notice: document.querySelector("#notice"),
    totalCount: document.querySelector("#total-count"),
    activeCount: document.querySelector("#active-count"),
    departmentCounts: document.querySelector("#department-counts"),
    tableBody: document.querySelector("#employee-table-body"),
    visibleCount: document.querySelector("#visible-count"),
    search: document.querySelector("#search-input"),
    departmentFilter: document.querySelector("#department-filter"),
    formPanel: document.querySelector("#employee-form-panel"),
    form: document.querySelector("#employee-form"),
    formTitle: document.querySelector("#form-title"),
    saveButton: document.querySelector("#save-button"),
    addButton: document.querySelector("#add-employee-button"),
    cancelButton: document.querySelector("#cancel-form-button"),
    cancelSecondary: document.querySelector("#cancel-form-secondary")
  };
  let employees = [];
  let noticeTimer;

  async function request(path, options = {}) {
    const response = await fetch(`${api}${path}`, {
      ...options,
      headers: { "Content-Type": "application/json", ...options.headers }
    });
    if (!response.ok) {
      let detail = `Request failed (${response.status})`;
      try {
        const body = await response.json();
        detail = body.detail || body.message || detail;
      } catch {
        // Keep the HTTP status message when the server response isn't JSON.
      }
      throw new Error(detail);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  function showNotice(message, type = "success") {
    window.clearTimeout(noticeTimer);
    elements.notice.textContent = message;
    elements.notice.className = `alert alert-${type} mb-4`;
    noticeTimer = window.setTimeout(() => elements.notice.classList.add("d-none"), 5000);
  }

  function appendCell(row, value, className = "") {
    const cell = document.createElement("td");
    if (className) cell.className = className;
    cell.textContent = value;
    row.append(cell);
    return cell;
  }

  function renderDashboard() {
    elements.totalCount.textContent = String(employees.length);
    elements.activeCount.textContent = String(employees.filter(employee => employee.active).length);

    const departments = new Map();
    employees.forEach(employee => departments.set(employee.department, (departments.get(employee.department) || 0) + 1));
    elements.departmentCounts.replaceChildren();
    [...departments.entries()].sort(([first], [second]) => first.localeCompare(second)).forEach(([name, count]) => {
      const item = document.createElement("span");
      item.append(document.createTextNode(`${name} `));
      const number = document.createElement("strong");
      number.textContent = String(count);
      item.append(number);
      elements.departmentCounts.append(item);
    });
    if (!departments.size) elements.departmentCounts.textContent = "No departments yet";

    const selectedDepartment = elements.departmentFilter.value;
    const departmentNames = [...new Set(employees.map(employee => employee.department))]
      .sort((first, second) => first.localeCompare(second));
    elements.departmentFilter.replaceChildren(new Option("All departments", ""));
    departmentNames.forEach(name => elements.departmentFilter.add(new Option(name, name)));
    if (departmentNames.includes(selectedDepartment)) elements.departmentFilter.value = selectedDepartment;
  }

  function renderEmployees() {
    const query = elements.search.value.trim().toLocaleLowerCase();
    const department = elements.departmentFilter.value;
    const filtered = employees.filter(employee =>
      employee.name.toLocaleLowerCase().includes(query) &&
      (!department || employee.department === department)
    );
    elements.visibleCount.textContent = String(filtered.length);
    elements.tableBody.replaceChildren();

    if (!filtered.length) {
      const row = document.createElement("tr");
      const cell = document.createElement("td");
      cell.className = "empty-state";
      cell.colSpan = 6;
      cell.textContent = employees.length ? "No employees match your search." : "No employees found. Add your first employee.";
      row.append(cell);
      elements.tableBody.append(row);
      return;
    }

    filtered.forEach(employee => {
      const row = document.createElement("tr");
      const identity = document.createElement("td");
      const name = document.createElement("div");
      name.className = "employee-name";
      name.textContent = employee.name;
      const email = document.createElement("div");
      email.className = "employee-email";
      email.textContent = employee.email;
      const id = document.createElement("div");
      id.className = "employee-id";
      id.textContent = `ID ${employee.id}`;
      identity.append(name, email, id);
      row.append(identity);
      appendCell(row, employee.department);
      appendCell(row, employee.designation);
      appendCell(row, new Intl.NumberFormat(undefined, {
        style: "currency", currency: "USD", maximumFractionDigits: 0
      }).format(employee.salary));
      const status = document.createElement("td");
      const badge = document.createElement("span");
      badge.className = `status-badge ${employee.active ? "status-active" : "status-inactive"}`;
      badge.textContent = employee.active ? "Active" : "Inactive";
      status.append(badge);
      row.append(status);

      const actions = document.createElement("td");
      actions.className = "text-end text-nowrap";
      const edit = document.createElement("button");
      edit.type = "button";
      edit.className = "action-button";
      edit.textContent = "Edit";
      edit.addEventListener("click", () => openForm(employee));
      const remove = document.createElement("button");
      remove.type = "button";
      remove.className = "action-button action-delete ms-2";
      remove.textContent = "Delete";
      remove.addEventListener("click", () => deleteEmployee(employee));
      actions.append(edit, remove);
      row.append(actions);
      elements.tableBody.append(row);
    });
  }

  async function refreshEmployees() {
    try {
      employees = await request("/employees");
      renderDashboard();
      renderEmployees();
    } catch (error) {
      elements.tableBody.replaceChildren();
      const row = document.createElement("tr");
      const cell = document.createElement("td");
      cell.className = "empty-state";
      cell.colSpan = 6;
      cell.textContent = `Could not load employees: ${error.message}`;
      row.append(cell);
      elements.tableBody.append(row);
      showNotice(`Unable to connect to the employee API: ${error.message}`, "danger");
    }
  }

  function openForm(employee) {
    elements.form.reset();
    elements.formPanel.classList.remove("d-none");
    elements.formTitle.textContent = employee ? "Edit employee" : "Add employee";
    elements.saveButton.textContent = employee ? "Save changes" : "Save employee";
    const idInput = elements.form.elements.id;
    idInput.disabled = Boolean(employee);
    if (employee) {
      idInput.value = employee.id;
      elements.form.elements.name.value = employee.name;
      elements.form.elements.email.value = employee.email;
      elements.form.elements.department.value = employee.department;
      elements.form.elements.designation.value = employee.designation;
      elements.form.elements.salary.value = employee.salary;
      elements.form.elements.active.checked = employee.active;
    }
    elements.formPanel.scrollIntoView({ behavior: "smooth", block: "start" });
    if (!employee) idInput.focus({ preventScroll: true });
  }

  function closeForm() {
    elements.form.reset();
    elements.form.elements.id.disabled = false;
    elements.formPanel.classList.add("d-none");
  }

  async function saveEmployee(event) {
    event.preventDefault();
    const formData = new FormData(elements.form);
    const employee = {
      id: Number(formData.get("id")),
      name: formData.get("name").trim(),
      email: formData.get("email").trim(),
      department: formData.get("department").trim(),
      designation: formData.get("designation").trim(),
      salary: Number(formData.get("salary")),
      active: elements.form.elements.active.checked
    };
    const editing = elements.form.elements.id.disabled;
    const path = editing ? `/employees/${employee.id}` : "/employees";
    elements.saveButton.disabled = true;
    try {
      await request(path, { method: editing ? "PUT" : "POST", body: JSON.stringify(employee) });
      closeForm();
      await refreshEmployees();
      showNotice(editing ? "Employee updated successfully." : "Employee added successfully.");
    } catch (error) {
      showNotice(error.message, "danger");
    } finally {
      elements.saveButton.disabled = false;
    }
  }

  async function deleteEmployee(employee) {
    if (!window.confirm(`Delete ${employee.name} (ID ${employee.id})? This cannot be undone.`)) return;
    try {
      await request(`/employees/${employee.id}`, { method: "DELETE" });
      await refreshEmployees();
      showNotice(`${employee.name} was deleted.`);
    } catch (error) {
      showNotice(error.message, "danger");
    }
  }

  elements.addButton.addEventListener("click", () => openForm());
  elements.cancelButton.addEventListener("click", closeForm);
  elements.cancelSecondary.addEventListener("click", closeForm);
  elements.form.addEventListener("submit", saveEmployee);
  elements.search.addEventListener("input", renderEmployees);
  elements.departmentFilter.addEventListener("change", renderEmployees);
  refreshEmployees();
})();

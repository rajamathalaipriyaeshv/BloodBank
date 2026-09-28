const API = "/api/bloodbank";

// ─── TOAST ──────────────────────────────────────────────────────────────────
let toastTimer;
function showToast(msg, type = "info") {
    const t = document.getElementById("toast");
    t.className = `toast show ${type}`;
    t.textContent = msg;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { t.className = "toast hidden"; }, 4000);
}

// ─── TAB NAVIGATION ─────────────────────────────────────────────────────────
function openTab(evt, tabName) {
    document.querySelectorAll(".tab-content").forEach(el => el.classList.remove("active-tab"));
    document.querySelectorAll(".tab-btn").forEach(el => el.classList.remove("active"));
    document.getElementById(tabName).classList.add("active-tab");
    evt.currentTarget.classList.add("active");
    // Auto-load inventory data when switching to that tab
    if (tabName === "inventoryTab") {
        loadStockSummary();
        loadExpiringSoon();
    }
}

// ─── HELPER: set result box ──────────────────────────────────────────────────
function setResult(id, html, type = "") {
    const el = document.getElementById(id);
    el.innerHTML = html;
    el.className = "result-box " + type;
}

// ─── HELPER: set table body ──────────────────────────────────────────────────
function setTableBody(tbodyId, html) {
    document.getElementById(tbodyId).innerHTML = html;
}

// ─── HELPER: expiry badge ────────────────────────────────────────────────────
function expiryBadge(unit) {
    const today = new Date();
    const expiry = new Date(unit.expiryDate);
    const diff = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24));
    if (unit.issued) return '<span class="badge badge-issued">Issued</span>';
    if (diff < 0)    return '<span class="badge badge-issued">Expired</span>';
    if (diff <= 7)   return `<span class="badge badge-expiring">Expires in ${diff}d</span>`;
    return '<span class="badge badge-available">Available</span>';
}

// ─── DONORS ─────────────────────────────────────────────────────────────────

async function registerDonor() {
    const name = document.getElementById("donorName").value.trim();
    const bloodGroup = document.getElementById("donorGroup").value;
    if (!name) { showToast("Please enter donor name.", "error"); return; }
    if (!bloodGroup) { showToast("Please select a blood group.", "error"); return; }

    const res = await fetch(`${API}/donors`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, bloodGroup })
    });
    const data = await res.json();
    if (res.ok) {
        showToast(`✅ Donor registered! ID: ${data.id}`, "success");
        document.getElementById("donorName").value = "";
        document.getElementById("donorGroup").value = "";
        loadDonors();
    } else {
        showToast("❌ " + (data.message || "Error registering donor."), "error");
    }
}

async function checkEligibility() {
    const id = document.getElementById("eligibilityDonorId").value;
    if (!id) { showToast("Enter a Donor ID.", "error"); return; }

    const res = await fetch(`${API}/donors/${id}/eligibility`);
    const data = await res.json();
    if (res.ok) {
        const cls = data.eligible ? "success" : "error";
        const icon = data.eligible ? "✅" : "❌";
        setResult("eligibilityResult",
            `${icon} <strong>${data.message}</strong>`, cls);
    } else {
        setResult("eligibilityResult", "❌ " + data.message, "error");
    }
}

async function loadDonors() {
    const res = await fetch(`${API}/donors`);
    const data = await res.json();
    if (!res.ok) { showToast("Failed to load donors.", "error"); return; }
    if (data.length === 0) {
        setTableBody("donorTableBody", `<tr><td colspan="4" class="empty-row">No donors registered yet.</td></tr>`);
        return;
    }
    setTableBody("donorTableBody", data.map(d =>
        `<tr>
            <td>${d.id}</td>
            <td>${d.name}</td>
            <td><span class="badge badge-available">${d.bloodGroup}</span></td>
            <td>${d.lastDonationDate || '<span class="muted">Never</span>'}</td>
        </tr>`
    ).join(""));
}

// ─── DONATIONS ───────────────────────────────────────────────────────────────

// Auto-fetch donor's blood group when Donor ID is typed
let fetchTimer;
async function fetchDonorBloodGroup() {
    const id = document.getElementById("donateDonorId").value;
    const display = document.getElementById("donorBloodGroupDisplay");

    if (!id || id <= 0) {
        display.textContent = "— enter donor ID above —";
        display.className = "blood-group-display muted";
        return;
    }

    // Debounce: wait 400ms after user stops typing
    clearTimeout(fetchTimer);
    fetchTimer = setTimeout(async () => {
        display.textContent = "Fetching...";
        display.className = "blood-group-display muted";

        const res = await fetch(`${API}/donors/${id}`);
        if (res.ok) {
            const donor = await res.json();
            display.innerHTML = `🩸 <strong>${donor.bloodGroup}</strong> &nbsp;(${donor.name})`;
            display.className = "blood-group-display loaded";
        } else {
            display.textContent = "❌ Donor not found";
            display.className = "blood-group-display error-state";
        }
    }, 400);
}

async function addDonation() {
    const donorId = document.getElementById("donateDonorId").value;
    const display = document.getElementById("donorBloodGroupDisplay");

    if (!donorId) { showToast("Enter a Donor ID.", "error"); return; }
    if (display.classList.contains("error-state") || display.classList.contains("muted")) {
        showToast("Please enter a valid Donor ID first.", "error");
        return;
    }

    const res = await fetch(`${API}/donate?donorId=${donorId}`, { method: "POST" });
    const data = await res.json();
    if (res.ok) {
        showToast(`✅ Donation logged! Blood Group: ${data.bloodGroup} | Donation ID: ${data.id}`, "success");
        document.getElementById("donateDonorId").value = "";
        display.textContent = "— enter donor ID above —";
        display.className = "blood-group-display muted";
        loadAllDonations();
    } else {
        showToast("❌ " + (data.message || "Donation failed."), "error");
    }
}

async function loadAllDonations() {
    const res = await fetch(`${API}/donations`);
    const data = await res.json();
    if (!res.ok) { showToast("Failed to load donations.", "error"); return; }
    if (data.length === 0) {
        setTableBody("donationTableBody", `<tr><td colspan="5" class="empty-row">No donations logged yet.</td></tr>`);
        return;
    }
    setTableBody("donationTableBody", data.map(d =>
        `<tr>
            <td>${d.id}</td>
            <td>${d.donor ? d.donor.name : "—"} (ID: ${d.donor ? d.donor.id : "—"})</td>
            <td><span class="badge badge-available">${d.bloodGroup}</span></td>
            <td>${d.donationDate}</td>
            <td>${d.bloodUnit ? d.bloodUnit.id : "—"}</td>
        </tr>`
    ).join(""));
}

async function loadDonationsByDonor() {
    const id = document.getElementById("donorDonationsId").value;
    if (!id) { showToast("Enter a Donor ID.", "error"); return; }

    const res = await fetch(`${API}/donors/${id}/donations`);
    const data = await res.json();
    if (!res.ok) { showToast("❌ " + data.message, "error"); return; }
    if (data.length === 0) {
        setTableBody("donationTableBody", `<tr><td colspan="5" class="empty-row">No donations found for Donor ID ${id}.</td></tr>`);
        return;
    }
    setTableBody("donationTableBody", data.map(d =>
        `<tr>
            <td>${d.id}</td>
            <td>${d.donor ? d.donor.name : "—"} (ID: ${d.donor ? d.donor.id : "—"})</td>
            <td><span class="badge badge-available">${d.bloodGroup}</span></td>
            <td>${d.donationDate}</td>
            <td>${d.bloodUnit ? d.bloodUnit.id : "—"}</td>
        </tr>`
    ).join(""));
    showToast(`Showing ${data.length} donation(s) for Donor ID ${id}.`, "info");
}

// ─── ISSUE BLOOD ─────────────────────────────────────────────────────────────

async function issueUnit() {
    const bloodGroup  = document.getElementById("issueGroup").value;
    const requestedBy = document.getElementById("requestedBy").value.trim() || "Unknown";
    if (!bloodGroup) { showToast("Select a blood group.", "error"); return; }

    const res = await fetch(
        `${API}/issue?bloodGroup=${encodeURIComponent(bloodGroup)}&requestedBy=${encodeURIComponent(requestedBy)}`,
        { method: "POST" }
    );
    const data = await res.json();
    if (res.ok) {
        showToast(`✅ Blood unit issued! Record ID: ${data.id}`, "success");
        setResult("issueResult",
            `<strong>Issue Record #${data.id}</strong><br>
             Blood Group: <span class="badge badge-available">${data.bloodGroup}</span><br>
             Unit ID: ${data.bloodUnit ? data.bloodUnit.id : "—"}<br>
             Date: ${data.issueDate}<br>
             Requested By: ${data.requestedBy}`,
            "success");
        document.getElementById("issueGroup").value = "";
        document.getElementById("requestedBy").value = "";
        loadIssueRecords();
    } else {
        showToast("❌ " + (data.message || "Issue failed."), "error");
        setResult("issueResult", "❌ " + data.message, "error");
    }
}

async function loadIssueRecords() {
    const res = await fetch(`${API}/issue-records`);
    const data = await res.json();
    if (!res.ok) { showToast("Failed to load issue records.", "error"); return; }
    if (data.length === 0) {
        setTableBody("issueTableBody", `<tr><td colspan="5" class="empty-row">No issue records yet.</td></tr>`);
        return;
    }
    setTableBody("issueTableBody", data.map(r =>
        `<tr>
            <td>${r.id}</td>
            <td>${r.bloodUnit ? r.bloodUnit.id : "—"}</td>
            <td><span class="badge badge-available">${r.bloodGroup}</span></td>
            <td>${r.issueDate}</td>
            <td>${r.requestedBy || "—"}</td>
        </tr>`
    ).join(""));
}

// ─── INVENTORY ───────────────────────────────────────────────────────────────

async function loadStockSummary() {
    const res = await fetch(`${API}/stock-summary`);
    const data = await res.json();
    if (!res.ok) { showToast("Failed to load stock.", "error"); return; }

    const grid = document.getElementById("stockGrid");
    const entries = Object.entries(data);
    if (entries.length === 0) {
        grid.innerHTML = `<p class="muted">No stock data available.</p>`;
        return;
    }
    grid.innerHTML = entries.map(([group, count]) =>
        `<div class="bg-card ${count === 0 ? 'zero' : ''}">
            <div class="bg-label">${group}</div>
            <span class="bg-count">${count}</span>
            <div class="bg-units">unit${count !== 1 ? 's' : ''}</div>
        </div>`
    ).join("");
}

async function loadExpiringSoon() {
    const res = await fetch(`${API}/expiring-soon`);
    const data = await res.json();
    if (!res.ok) { showToast("Failed to load expiring units.", "error"); return; }
    if (data.length === 0) {
        setTableBody("expiringTableBody", `<tr><td colspan="4" class="empty-row">No units expiring within 7 days. ✅</td></tr>`);
        return;
    }
    setTableBody("expiringTableBody", data.map(u =>
        `<tr>
            <td>${u.id}</td>
            <td><span class="badge badge-available">${u.bloodGroup}</span></td>
            <td>${u.collectionDate}</td>
            <td><span class="badge badge-expiring">${u.expiryDate}</span></td>
        </tr>`
    ).join(""));
    showToast(`⚠️ ${data.length} unit(s) expiring within 7 days!`, "error");
}

async function loadAllBloodUnits() {
    const res = await fetch(`${API}/blood-units`);
    const data = await res.json();
    if (!res.ok) { showToast("Failed to load blood units.", "error"); return; }
    if (data.length === 0) {
        setTableBody("bloodUnitTableBody", `<tr><td colspan="4" class="empty-row">No blood units in system.</td></tr>`);
        return;
    }
    setTableBody("bloodUnitTableBody", data.map(u =>
        `<tr>
            <td>${u.id}</td>
            <td><span class="badge badge-available">${u.bloodGroup}</span></td>
            <td>${u.expiryDate}</td>
            <td>${expiryBadge(u)}</td>
        </tr>`
    ).join(""));
}

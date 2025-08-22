(function () {
  "use strict";

  // ---- Preset templates ----
  const TEMPLATES = {
    jobApplication: [
      { title: "Personal Information", fields: [
        { name:"name", label:"Full Name", type:"text" },
        { name:"email", label:"Email Address", type:"email" },
        { name:"phone", label:"Phone Number", type:"tel" },
        { name:"address", label:"Address", type:"text" }
      ]},
      { title: "Education", fields: [
        { name:"degree", label:"Highest Degree", type:"text" },
        { name:"institution", label:"Institution", type:"text" },
        { name:"gradYear", label:"Graduation Year", type:"number" }
      ]},
      { title: "Work Experience", fields: [
        { name:"company", label:"Company", type:"text" },
        { name:"jobTitle", label:"Job Title", type:"text" },
        { name:"years", label:"Years Exp.", type:"number" }
      ]}
    ],
    eventRegistration: [
      { title: "Event Details", fields: [
        { name:"eventDate", label:"Date", type:"date" },
        { name:"location", label:"Location", type:"text" }
      ]},
      { title: "Attendee Info", fields: [
        { name:"name", label:"Name", type:"text" },
        { name:"email", label:"Email", type:"email" },
        { name:"ticket", label:"Ticket Type", type:"select", options:["Standard","VIP"] }
      ]},
      { title: "Add-ons", fields: [
        { name:"meal", label:"Meal Preference", type:"select", options:["None","Veg","Non-Veg"] },
        { name:"workshop", label:"Workshop", type:"select", options:["A","B","C"] }
      ]},
      { title: "Payment", fields: [
        { name:"cardNumber", label:"Card Number", type:"text" },
        { name:"expiry", label:"Expiry Date", type:"month" }
      ]}
    ],
    customerSurvey: [
      { title: "Product Used", fields: [
        { name:"product", label:"Product", type:"select", options:["A","B","C"] }
      ]},
      { title: "Rating", fields: [
        { name:"rating", label:"Satisfaction (1–5)", type:"number", min:1, max:5 }
      ]},
      { title: "Comments", fields: [
        { name:"comments", label:"Comments", type:"textarea" }
      ]},
      { title: "Follow-up Consent", fields: [
        { name:"consent", label:"May we contact you?", type:"select", options:["Yes","No"] }
      ]}
    ],
  };

  const DEFAULT_CUSTOM_FLOW = [
    {
      title: "Basics",
      fields: [
        { name: "title",        label: "Title",           type: "text" },
        { name: "ownerEmail",   label: "Owner Email",     type: "email" },
        { name: "website",      label: "Website",         type: "url",   placeholder: "https://example.com" }
      ]
    },
    {
      title: "Details",
      fields: [
        { name: "category",     label: "Category",        type: "select", options: ["A", "B", "C"] },
        { name: "quantity",     label: "Quantity",        type: "number", min: 1 },
        { name: "region",       label: "Data Region",     type: "select", options: ["US", "EU", "APAC"] }
      ]
    },
    {
      title: "Notes",
      fields: [
        { name: "summary",      label: "Short Summary",   type: "text" },
        { name: "notes",        label: "Additional Notes",type: "textarea" }
      ]
    }
  ];
  
  const STORAGE_KEY  = "aemWizardSubmissions";
  const SESSION_CUST = "aemWizardCustomTemp";

  function onReady() {
    const root = document.querySelector(".job-application");
    if (!root) return;

    const form        = root.querySelector("form.wizard-form");
    const container   = root.querySelector(".steps-container");
    const progress    = root.querySelector(".progress");
    const bar         = progress.querySelector(".progress-bar");
    const submitLabel = root.dataset.submitLabel || "Submit";
    const viewMode    = (root.dataset.view || "").toLowerCase();

    // Custom (authored or session-built)
    let authoredCustomName = (root.dataset.customName || "").trim();
    let authoredCustomFlow = safeParseSteps(root.dataset.customFlow || "");
    try {
      if ((!authoredCustomName || !authoredCustomFlow.length) && sessionStorage.getItem(SESSION_CUST)) {
        const temp = JSON.parse(sessionStorage.getItem(SESSION_CUST));
        if (temp && temp.name && Array.isArray(temp.flow) && temp.flow.length) {
          authoredCustomName = temp.name;
          authoredCustomFlow = temp.flow;
        }
      }
    } catch (_) {}

    if (viewMode === "submissions") {
      progress.style.display = "none";
      form.style.display = "none";
      const holder = ensureSubmissionsHolder(root);
      renderSubmissionsView(holder);
      return;
    }

    let flow = [];
    let total = 0;
    let current = 0;

    progress.style.display = "none";
    buildSelector();

    function buildSelector() {
      container.innerHTML = `
        <div class="selector-card">
          <h3>Select Use Case</h3>
          <label for="wizard-choice" class="sr-only">Which form?</label>
          <select id="wizard-choice" required>
            <option value="">— choose —</option>
            <option value="jobApplication">Job Application</option>
            <option value="eventRegistration">Event Registration</option>
            <option value="customerSurvey">Customer Survey</option>
            <option value="__custom">Custom...</option>
          </select>
          <div class="buttons">
            <button type="button" id="continue-btn" class="btn-primary" disabled>Continue</button>
            ${authoredCustomFlow.length ? `<button type="button" id="rebuild-custom" class="btn-secondary">Edit Custom…</button>` : ""}
          </div>
        </div>
      `;

      const sel = container.querySelector("#wizard-choice");
      const btn = container.querySelector("#continue-btn");

      sel.addEventListener("change", () => {
        btn.disabled = !sel.value;
        setTitle(sel.value === "__custom" ? "Create Custom Wizard" : valueToDisplayName(sel.value));
      });

      btn.addEventListener("click", (e) => {
        e.stopImmediatePropagation();
        if (sel.value === "__custom") {
          setTitle("Create Custom Wizard");
          openCustomBuilder(true);
        } else {
          setTitle(valueToDisplayName(sel.value));
          flow = TEMPLATES[sel.value] || [];
          total = flow.length + 1; buildFlow();
        }
      });

      const rebuildBtn = container.querySelector("#rebuild-custom");
      if (rebuildBtn) {
        rebuildBtn.addEventListener("click", (e) => {
          e.stopImmediatePropagation();
          openCustomBuilder();
        });
      }
    }

    function openCustomBuilder(fresh = false) {
      const savedName = fresh ? "" : (authoredCustomName || "").trim();
      const txtForArea = fresh
        ? JSON.stringify(DEFAULT_CUSTOM_FLOW, null, 2)
        : (authoredCustomFlow.length
            ? JSON.stringify(authoredCustomFlow, null, 2)
            : JSON.stringify(DEFAULT_CUSTOM_FLOW, null, 2));

      setTitle(fresh ? "Create Custom Wizard" : (savedName || "Edit Custom Wizard"));

      container.innerHTML = `
        <div class="selector-card">
          <h3>${fresh ? "Create Custom Wizard" : "Edit Custom Wizard"}</h3>

          <div class="field field--full">
            <label>Wizard Name</label>
            <input type="text" id="cust-name" placeholder="e.g., Partner Signup" value="${escapeAttr(savedName)}" required />
          </div>

          <div class="field field--full">
            <label>Custom Flow JSON</label>
            <textarea id="cust-json" rows="16">${escapeHtml(txtForArea)}</textarea>
            <small>Paste or edit an array of steps; each step has <code>title</code> and <code>fields</code>.</small>
          </div>

          <div class="buttons">
            <button type="button" class="prev-btn btn-secondary">Back</button>
            ${fresh ? "" : `<button type="button" id="copy-json" class="btn-secondary">Copy JSON</button>`}
            <button type="button" id="format-json" class="btn-secondary">Format JSON</button>
            <button type="button" id="build-custom" class="btn-primary">${fresh ? "Build" : "Update"}</button>
          </div>
        </div>
      `;

      const nameEl  = container.querySelector("#cust-name");
      const jsonEl  = container.querySelector("#cust-json");
      const buildEl = container.querySelector("#build-custom");

      function validateBuilder() {
        const nm = (nameEl.value || "").trim();
        let ok = false;
        try { ok = Array.isArray(JSON.parse(jsonEl.value)) && JSON.parse(jsonEl.value).length > 0; } catch (_) { ok = false; }
        buildEl.disabled = !(nm && ok);
        setTitle(nm || (fresh ? "Create Custom Wizard" : (savedName || "Edit Custom Wizard")));
      }

      validateBuilder();

      nameEl.addEventListener("input", validateBuilder);
      jsonEl.addEventListener("input", validateBuilder);

      container.addEventListener("click", builderHandler);

      function builderHandler(ev) {
        if (ev.target.matches(".prev-btn")) {
          ev.stopImmediatePropagation();
          container.removeEventListener("click", builderHandler);
          buildSelector();
        }

        if (ev.target.matches("#copy-json")) {
          ev.stopImmediatePropagation();
          navigator.clipboard?.writeText(jsonEl.value).then(() => {
            ev.target.textContent = "Copied!";
            setTimeout(() => (ev.target.textContent = "Copy JSON"), 1200);
          }).catch(() => alert("Clipboard not available. Copy manually."));
        }

        if (ev.target.matches("#format-json")) {
          ev.stopImmediatePropagation();
          try { jsonEl.value = JSON.stringify(JSON.parse(jsonEl.value), null, 2); }
          catch { alert("That isn’t valid JSON yet. Fix errors and try Format again."); }
          validateBuilder();
        }

        if (ev.target.matches("#build-custom")) {
          ev.stopImmediatePropagation();
          const nm  = (nameEl.value || "").trim();
          let parsed;
          try { parsed = JSON.parse(jsonEl.value); } catch { parsed = []; }
          if (!nm) return;
          if (!Array.isArray(parsed) || !parsed.length) return;

          authoredCustomName = nm;
          authoredCustomFlow = parsed;
          setTitle(nm);
          try { sessionStorage.setItem(SESSION_CUST, JSON.stringify({ name: nm, flow: parsed })); } catch {}

          flow = authoredCustomFlow;
          total = flow.length + 1;
          container.removeEventListener("click", builderHandler);
          buildFlow();
        }
      }
    }

    function buildFlow() {
      progress.style.display = "";
      bar.style.width = "0%";
      progress.setAttribute("aria-valuemax", total);
      progress.setAttribute("aria-valuenow", 1);

      container.innerHTML = "";

      flow.forEach((step, i) => {
        const idx = i + 1;
        const el = document.createElement("div");
        el.className = `step step-${idx}`;
        el.setAttribute("role", "group");
        el.setAttribute("aria-labelledby", `step${idx}-title`);
        el.innerHTML = `
          <h3 id="step${idx}-title">${escapeHtml(step.title || `Step ${idx}`)}</h3>
          <div class="fields-grid">
            ${step.fields.map(renderField).join("")}
          </div>
          <div class="buttons">
            ${i > 0 ? `<button type="button" class="prev-btn btn-secondary">Back</button>` : ""}
            <button type="button" class="next-btn btn-primary" disabled>Next</button>
          </div>
        `;
        container.appendChild(el);
      });

      const rev = document.createElement("div");
      rev.className = `step step-${total}`;
      rev.setAttribute("role", "group");
      rev.setAttribute("aria-labelledby", `step${total}-title`);
      rev.innerHTML = `
        <h3 id="step${total}-title">Review &amp; Submit</h3>
        <div class="review-wrap">
          ${flow.map((s, i) => `
            <section class="review-card">
              <header>
                <span class="badge">Step ${i+1}</span>
                <h4>${escapeHtml(s.title || `Step ${i+1}`)}</h4>
              </header>
              <div class="review-grid" data-step-index="${i+1}"></div>
            </section>
          `).join("")}
        </div>
        <div class="buttons">
          <button type="button" class="prev-btn btn-secondary">Back</button>
          <button type="submit" class="submit-btn btn-accent">${escapeHtml(submitLabel)}</button>
        </div>
      `;
      container.appendChild(rev);

      attachNav();
      current = 0;
      showCurrent();

      form.addEventListener("submit", onSubmitOnce, { once: true });
    }

    form.addEventListener("blur", (e) => {
      if (e.target && e.target.matches('input[type="url"]')) {
        const v = e.target.value.trim();
        if (v && !/^[a-z][a-z0-9+.\-]*:\/\//i.test(v)) {
          e.target.value = "https://" + v;
        }
        validateCurrent();
      }
    }, true);

    function onSubmitOnce(e) {
      e.preventDefault();
      e.stopImmediatePropagation();
      const payload = collectValues();
      const chosenName = detectChosenName();
      saveSubmission({ wizard: chosenName, when: new Date().toISOString(), data: payload });

      container.innerHTML = `
        <div class="thanks-card">
          <h3>Thanks!</h3>
          <p>Your <strong>${escapeHtml(chosenName)}</strong> submission was recorded.</p>
          <div class="buttons">
            <button type="button" class="view-submissions btn-secondary">View Submissions</button>
            <button type="button" class="back-start btn-primary">Back to Start</button>
          </div>
        </div>
      `;
      container.querySelector(".view-submissions").addEventListener("click", (ev) => {
        ev.stopImmediatePropagation(); showSubmissions();
      });
      container.querySelector(".back-start").addEventListener("click", (ev) => {
        ev.stopImmediatePropagation(); backToStart();
      });
    }

    function backToStart() {
      const holder = root.querySelector(".submissions-view");
      if (holder) holder.remove();
      form.style.display = "";
      progress.style.display = "none";
      setTitle(null);
      buildSelector();
    }

    function showSubmissions() {
      const holder = ensureSubmissionsHolder(root);
      form.style.display = "none";
      progress.style.display = "none";
      holder.hidden = false;
      renderSubmissionsView(holder);
    }

    function ensureSubmissionsHolder(rootEl) {
      let holder = rootEl.querySelector(".submissions-view");
      if (!holder) {
        holder = document.createElement("div");
        holder.className = "submissions-view";
        rootEl.appendChild(holder);
      }
      return holder;
    }

    // ---------- Submissions list (group, filter, delete) ----------
    function renderSubmissionsView(holder) {
      const all = loadSubmissions();
      const wizards = unique(all.map(x => x.wizard || "Wizard")).sort();
      const currentFilter = holder.dataset.filter || "All";

      // toolbar
      holder.innerHTML = `
        <div class="subs-card">
          <header class="subs-toolbar">
            <h3>Submissions</h3>
            <div class="subs-actions">
              <label class="sr-only" for="wizard-filter">Filter</label>
              <select id="wizard-filter" class="subs-filter">
                <option ${currentFilter==="All"?"selected":""}>All</option>
                ${wizards.map(w => `<option ${w===currentFilter?"selected":""}>${escapeHtml(w)}</option>`).join("")}
              </select>
              <button type="button" class="subs-delete-all btn-danger" ${all.length? "":"disabled"}>Delete All</button>
              <button type="button" class="subs-back btn-primary">Back to Start</button>
            </div>
          </header>
          ${renderGroups(all, currentFilter)}
        </div>
      `;

      // events
      holder.querySelector("#wizard-filter").addEventListener("change", (e) => {
        holder.dataset.filter = e.target.value || "All";
        renderSubmissionsView(holder);
      });

      holder.querySelector(".subs-delete-all").addEventListener("click", () => {
        if (!loadSubmissions().length) return;
        if (confirm("Delete ALL submissions? This cannot be undone.")) {
          saveAllSubmissions([]);
          renderSubmissionsView(holder);
        }
      });

      holder.querySelector(".subs-back").addEventListener("click", () => backToStart());

      // per-item delete via delegation
      holder.addEventListener("click", (e) => {
        const btn = e.target.closest(".subs-delete");
        if (!btn) return;
        const id = btn.getAttribute("data-id");
        if (!id) return;
        const next = loadSubmissions().filter(s => String(s.id) !== String(id));
        saveAllSubmissions(next);
        renderSubmissionsView(holder);
      });

      // expand/collapse items
      holder.addEventListener("click", (e) => {
        if (e.target.closest(".subs-item header")) {
          const card = e.target.closest(".subs-item");
          card.classList.toggle("open");
        }
      });
    }

    function renderGroups(all, filter) {
      const filtered = (filter && filter !== "All") ? all.filter(x => x.wizard === filter) : all;
      if (!filtered.length) {
        return `<p class="muted empty">No submissions${filter && filter!=="All" ? ` for “${escapeHtml(filter)}”`:""}.</p>`;
      }
      const byWizard = groupBy(filtered, s => s.wizard || "Wizard");
      return `
        <div class="subs-groups">
          ${Object.keys(byWizard).sort().map(wiz => `
            <section class="subs-group">
              <h4 class="group-title">${escapeHtml(wiz)}</h4>
              <div class="subs-list">
                ${byWizard[wiz].map(renderSubmissionItem).join("")}
              </div>
            </section>
          `).join("")}
        </div>
      `;
    }

    function renderSubmissionItem(item) {
      const when = new Date(item.when || Date.now()).toLocaleString();
      const rows = Object.entries(item.data || {}).map(([k,v]) => `
        <div class="review-row">
          <div class="review-label">${escapeHtml(toLabel(k))}</div>
          <div class="review-value">${escapeHtml(String(v || ""))}</div>
        </div>
      `).join("");
      return `
        <article class="subs-item">
          <header>
            <h5>${escapeHtml(item.wizard || "Wizard")} submission</h5>
            <time>${escapeHtml(when)}</time>
            <button type="button" class="subs-delete btn-danger-outline" data-id="${escapeAttr(item.id)}">Delete</button>
          </header>
          <div class="subs-body">
            <div class="review-grid">${rows}</div>
          </div>
        </article>
      `;
    }

    // ---------- Wizard internals ----------
    function renderField(f) {
      const name  = escapeAttr(f.name || "");
      const label = escapeHtml(f.label || name || "Field");
      const type  = (f.type || "text").toLowerCase();
      const placeholder = f.placeholder ? ` placeholder="${escapeAttr(f.placeholder)}"` : "";
      const pattern = f.pattern ? ` pattern="${escapeAttr(f.pattern)}"` : "";

      if (type === "select") {
        const opts = Array.isArray(f.options) ? f.options : [];
        return `
          <div class="field">
            <label>${label}</label>
            <select name="${name}" required>
              <option value="">—</option>
              ${opts.map(o => `<option>${escapeHtml(String(o))}</option>`).join("")}
            </select>
          </div>`;
      }
      if (type === "textarea") {
        return `
          <div class="field field--full">
            <label>${label}</label>
            <textarea name="${name}" required></textarea>
          </div>`;
      }
      const min = f.min != null ? ` min="${String(f.min)}"` : "";
      const max = f.max != null ? ` max="${String(f.max)}"` : "";
      return `
        <div class="field">
          <label>${label}</label>
          <input type="${escapeAttr(type)}" name="${name}"${min}${max}${pattern}${placeholder} required/>
        </div>`;
    }

    function showCurrent() {
      const steps = container.querySelectorAll(".step");
      steps.forEach((el, i) => el.classList.toggle("active", i === current));
      updateProgress();
      validateCurrent();
      if (current === total - 1) populateReview();
    }

    function updateProgress() {
      const pct = ((current + 1) / total) * 100;
      bar.style.width = pct + "%";
      progress.setAttribute("aria-valuenow", current + 1);
    }

    function validateCurrent() {
      const stepEl = container.querySelectorAll(".step")[current];
      const next = stepEl.querySelector(".next-btn");
      if (!next) return;
      const ok = Array.from(stepEl.querySelectorAll("[required]")).every(i => i.checkValidity());
      next.disabled = !ok;
    }

    function attachNav() {
      container.querySelectorAll(".step").forEach((stepEl, idx) => {
        stepEl.querySelectorAll("[required]").forEach(inp => {
          const revalidate = () => { if (idx === current) validateCurrent(); };
          inp.addEventListener("input", revalidate);
          inp.addEventListener("change", revalidate);
          inp.addEventListener("blur", revalidate, true);
        });
      });

      container.addEventListener("click", (e) => {
        if (e.target.matches(".next-btn")) {
          e.stopImmediatePropagation();
          current = Math.min(current + 1, total - 1);
          showCurrent();
        }
        if (e.target.matches(".prev-btn")) {
          e.stopImmediatePropagation();
          current = Math.max(current - 1, 0);
          showCurrent();
        }
      });
    }

    function populateReview() {
      const payload = collectValues();
      flow.forEach((s, i) => {
        const grid = container.querySelector(`.review-grid[data-step-index="${i+1}"]`);
        if (!grid) return;
        grid.innerHTML = s.fields.map(f => {
          const key = f.name;
          const val = payload[key] || "";
          return `
            <div class="review-row">
              <div class="review-label">${escapeHtml(toLabel(key))}</div>
              <div class="review-value">${escapeHtml(val)}</div>
            </div>
          `;
        }).join("");
      });
    }

    function collectValues() {
      const data = {};
      form.querySelectorAll("input[name], select[name], textarea[name]").forEach(el => {
        data[el.name] = el.value;
      });
      return data;
    }

    function detectChosenName() {
      for (const [k, v] of Object.entries(TEMPLATES)) {
        if (v === flow) return toLabel(k);
      }
      if (flow === authoredCustomFlow) return authoredCustomName || "Custom";
      return "Wizard";
    }

    // ---------- Storage helpers ----------
    function saveSubmission(sub) {
      const rec = Object.assign({ id: genId() }, sub);
      const arr = loadSubmissions();
      arr.unshift(rec);
      saveAllSubmissions(arr);
    }
    function loadSubmissions() {
      try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]"); }
      catch(_) { return []; }
    }
    function saveAllSubmissions(arr) {
      try { localStorage.setItem(STORAGE_KEY, JSON.stringify(arr)); } catch(_) {}
    }
    function genId() {
      return Date.now().toString(36) + "-" + Math.random().toString(36).slice(2,8);
    }

    // ---------- Utilities ----------
    function groupBy(arr, keyFn) {
      return arr.reduce((acc, x) => {
        const k = keyFn(x);
        (acc[k] ||= []).push(x);
        return acc;
      }, {});
    }
    function unique(arr) {
      return Array.from(new Set(arr));
    }
    function safeParseSteps(txt) {
      if (!txt) return [];
      try {
        const arr = JSON.parse(txt);
        if (!Array.isArray(arr)) return [];
        return arr.map((s,i)=>({ title: s.title || `Step ${i+1}`, fields: Array.isArray(s.fields)? s.fields : [] }))
                  .filter(s => s.fields.length>0);
      } catch(_) { return []; }
    }
    function toLabel(s) {
      return String(s).replace(/([A-Z])/g," $1").replace(/[_\-]/g," ")
        .replace(/\b\w/g,c=>c.toUpperCase()).trim();
    }
    function escapeHtml(str) {
      return String(str).replace(/[&<>"']/g, s =>
        ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;" }[s]));
    }
    function escapeAttr(str) { return escapeHtml(str); }
    function setTitle(name) {
      const h = root.querySelector(".ja-title");
      if (!h) return;
      h.textContent = name || h.dataset.defaultTitle || "Wizard Stepper Forms";
    }

    function valueToDisplayName(val) {
      const map = {
        jobApplication: "Job Application",
        eventRegistration: "Event Registration",
        customerSurvey: "Customer Survey",
        "__custom": authoredCustomName || "Create Custom Wizard"
      };
      return map[val] || "Wizard Stepper Forms";
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", onReady);
  } else {
    onReady();
  }
})();

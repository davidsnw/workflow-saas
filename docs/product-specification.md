# WorkFlow SaaS — Product Specification

## 1. Product Overview

### 1.1 Purpose

WorkFlow SaaS is a multi-tenant field-service and maintenance management platform designed for small and mid-sized service companies.

The platform provides a centralized system for managing customers, service requests, work orders, technicians, scheduling, equipment, inventory, maintenance activities, invoicing, notifications, and operational reporting.

The goal is to replace fragmented workflows based on spreadsheets, calendars, phone calls, email, and messaging applications with a structured business workflow.

### 1.2 Core Business Workflow

The primary workflow is:

**Problem reported → Service Request → Review & Prioritization → Work Order → Technician Assignment → Scheduling → Field Work → Completion → Customer Confirmation → Invoicing → Notifications & Audit**

Each stage is represented by explicit business data and state transitions rather than being treated as an independent CRUD operation.

### 1.3 Multi-Tenancy

WorkFlow SaaS is a multi-tenant application.

Each subscribing company is represented as a **Tenant**. A tenant's users, customers, locations, equipment, service requests, work orders, inventory, invoices, documents, notifications, and other business data belong to that tenant.

Tenant isolation is a fundamental security requirement.

A user belonging to one tenant must never be able to access, modify, search, cache, receive, or infer data belonging to another tenant.

Tenant isolation must be enforced consistently across:

- REST APIs
- database queries
- background jobs
- caching
- messaging
- file storage
- WebSockets
- reporting
- audit logs
- notifications

### 1.4 SaaS Subscription Model

WorkFlow SaaS operates as a subscription-based SaaS platform.

Tenants subscribe to a plan that determines available features and usage limits.

Initial subscription plans:

- Starter
- Professional
- Enterprise

A subscription has a lifecycle such as:

**TRIAL → ACTIVE → PAST_DUE → SUSPENDED → CANCELLED**

Subscription state can affect access to platform functionality and must therefore be enforced by the backend.

### 1.5 Initial Product Scope

The initial version of the platform will focus on:

- Tenant registration
- Authentication and authorization
- User and role management
- Customer management
- Customer locations
- Equipment/assets
- Service requests
- Work orders
- Technician management
- Basic scheduling
- Tenant isolation
- Operational dashboards

Additional functionality such as inventory, maintenance plans, invoicing, notifications, file storage, event-driven processing, and subscription billing will be introduced incrementally.

## 2. Actors and Roles

WorkFlow SaaS supports multiple types of users. Each actor interacts with a different part of the platform and has different responsibilities and permissions.

### 2.1 Platform Administrator

The Platform Administrator operates the WorkFlow SaaS platform itself.

Responsibilities include:

- Managing tenants
- Managing subscription plans
- Managing tenant subscriptions
- Monitoring platform-level activity
- Managing platform configuration
- Reviewing platform-level audit and security information
- Monitoring system health and operational metrics

The Platform Administrator operates outside the normal tenant business workflow.

Platform Administrators must not be treated as ordinary tenant users.

### 2.2 Tenant Administrator

The Tenant Administrator manages a company's WorkFlow SaaS account.

Responsibilities include:

- Managing tenant users
- Assigning roles and permissions
- Managing company settings
- Managing customers
- Managing locations
- Managing equipment/assets
- Managing subscription-related settings
- Reviewing tenant-level activity and audit information

The Tenant Administrator can manage resources belonging to their own tenant but must never access resources belonging to another tenant.

### 2.3 Manager / Dispatcher

The Manager or Dispatcher manages day-to-day service operations.

Responsibilities include:

- Reviewing incoming service requests
- Validating and prioritizing requests
- Assigning technicians
- Scheduling work orders
- Monitoring technician workload
- Tracking active work
- Handling escalations
- Reviewing completed work
- Monitoring operational performance

The Manager/Dispatcher is primarily responsible for coordinating work rather than performing field work.

### 2.4 Technician

The Technician performs the actual field-service work.

The Technician interface focuses on assigned work and field operations.

Responsibilities include:

- Viewing assigned work orders
- Viewing job details and location information
- Starting work
- Pausing work
- Completing work
- Recording labor time
- Recording work performed
- Adding notes
- Recording measurements or observations
- Uploading job photos
- Recording parts/materials used
- Reporting additional problems discovered during the job
- Requesting assistance or escalation
- Recording equipment condition
- Participating in customer completion confirmation

A Technician should only have access to the business information required to perform their work.

### 2.5 Customer Administrator

The Customer Administrator represents a customer organization using the platform's customer-facing functionality.

Responsibilities may include:

- Managing customer users
- Managing customer locations
- Viewing equipment/assets
- Viewing service requests
- Viewing work orders
- Reviewing service history
- Viewing invoices and relevant financial information

The Customer Administrator is distinct from the WorkFlow SaaS Tenant Administrator.

### 2.6 Requester / Issuer

The Requester is the person who reports a problem or requests a service.

Responsibilities include:

- Creating service requests
- Describing the problem
- Selecting or identifying the affected location
- Identifying affected equipment when applicable
- Uploading supporting photos or documents
- Adding comments
- Viewing the status of their requests
- Receiving relevant notifications

The Requester does not automatically have permission to approve completed work or manage the customer's account.

### 2.7 Approver

The Approver is a user authorized to confirm that completed work has been satisfactorily performed.

The Approver may be different from the person who originally reported the problem.

Responsibilities may include:

- Reviewing completed work
- Reviewing technician notes
- Reviewing recorded parts and labor
- Confirming completion
- Rejecting or requesting clarification on completed work

This distinction is important because the person who reports a problem is not necessarily the person authorized to approve the resulting work.

### 2.8 Role and Permission Model

Actors describe business responsibilities, while roles and permissions define what a user can actually do within the application.

A user may have one or more roles depending on the tenant's configuration.

Permissions should be expressed as specific capabilities rather than relying exclusively on broad role checks.

Examples include:

- `SERVICE_REQUEST_CREATE`
- `SERVICE_REQUEST_VIEW`
- `SERVICE_REQUEST_UPDATE`
- `WORK_ORDER_CREATE`
- `WORK_ORDER_ASSIGN`
- `WORK_ORDER_SCHEDULE`
- `WORK_ORDER_START`
- `WORK_ORDER_COMPLETE`
- `CUSTOMER_MANAGE`
- `EQUIPMENT_MANAGE`
- `USER_MANAGE`
- `INVOICE_VIEW`
- `REPORT_VIEW`

Authorization must consider both:

1. **What the user is allowed to do**
2. **Which tenant and resources the user is allowed to access**

A valid permission must never override tenant isolation.

## 3. Service Request Lifecycle

A Service Request represents a reported problem, maintenance need, or request for service.

A Service Request is the starting point of the operational workflow.

### 3.1 Creation

A Service Request can be created by an authorized Requester or by an internal tenant user on behalf of a customer.

A request should contain enough information for the service team to understand the initial problem.

Typical information includes:

- Requester
- Customer
- Customer location
- Equipment/asset, when applicable
- Category
- Priority
- Description
- Attachments
- Creation timestamp

The system assigns the initial status automatically.

### 3.2 Service Request States

The initial lifecycle is:

**NEW → UNDER_REVIEW → APPROVED → CONVERTED_TO_WORK_ORDER**

A request may also enter:

**REJECTED**
**CANCELLED**

Therefore, the conceptual state model is:

```text
                    ┌───────────┐
                    │    NEW    │
                    └─────┬─────┘
                          │
                          ▼
                  ┌───────────────┐
                  │ UNDER_REVIEW  │
                  └───────┬───────┘
                          │
                 ┌────────┴────────┐
                 │                 │
                 ▼                 ▼
             REJECTED          APPROVED
                                   │
                                   ▼
                         CONVERTED_TO_WORK_ORDER

NEW / UNDER_REVIEW / APPROVED
                 │
                 ▼
             CANCELLED
```

The exact transition rules will be defined separately from the status values.

### 3.3 NEW

`NEW` means the request has been submitted but has not yet been reviewed by the service team.

Typical actions:

- View request
- Add information
- Add attachments
- Add comments
- Assign or change priority, if authorized
- Move request into review

### 3.4 UNDER_REVIEW

`UNDER_REVIEW` means a Manager/Dispatcher or another authorized internal user is evaluating the request.

The reviewer may:

- Verify the reported problem
- Confirm the customer and location
- Identify affected equipment
- Adjust priority
- Determine required skills
- Add internal notes
- Request additional information
- Reject the request
- Approve the request for service

### 3.5 APPROVED

`APPROVED` means the request has been accepted as a valid service requirement.

An approved request can be converted into a Work Order.

Approval does not necessarily mean that a technician has already been assigned.

### 3.6 REJECTED

`REJECTED` means the request will not result in service under the current request.

The system should preserve:

- Rejection reason
- User who rejected the request
- Timestamp
- Relevant comments

Rejected requests remain part of the historical record.

### 3.7 CANCELLED

A request may be cancelled when service is no longer required.

Cancellation should preserve:

- Cancellation reason
- User who cancelled the request
- Timestamp

Cancellation is different from rejection.

A cancellation means the request was withdrawn or is no longer needed, while rejection means the service organization decided not to accept the request.

### 3.8 Conversion to Work Order

An approved Service Request can be converted into a Work Order.

The Work Order represents the executable operational task.

The conversion should preserve the relationship between the two objects:

```text
ServiceRequest
      │
      ▼
WorkOrder
```

The Work Order should reference the originating Service Request rather than duplicating the entire request as unrelated data.

### 3.9 Request History

Important changes to a Service Request should be recorded.

Examples include:

- Status changes
- Priority changes
- Assignment changes
- Customer/location changes
- Equipment changes
- Comments
- Attachments
- Approval/rejection
- Cancellation

The system should maintain enough historical information to answer:

> Who changed this request, what changed, and when?

This history will later integrate with the platform's audit/event system.

### 3.10 Authorization

Service Request actions must be authorization-controlled.

Examples:

- Requesters may create and view their own requests.
- Managers/Dispatchers may review and manage operational requests.
- Tenant Administrators may manage requests according to their permissions.
- Customer users may access requests belonging to their customer organization according to their permissions.
- Platform Administrators operate at the platform level and do not automatically gain ordinary tenant business permissions.

Authorization must always be combined with tenant and resource-level isolation.

### 3.11 Business Rules

Initial business rules:

1. A Service Request belongs to exactly one tenant.
2. A Service Request belongs to a customer.
3. A Service Request has a requester.
4. A Service Request may reference a customer location.
5. A Service Request may reference equipment.
6. A Service Request has exactly one current lifecycle status.
7. Status transitions must be validated by the backend.
8. Rejected and cancelled requests remain historical records.
9. An approved request may be converted into a Work Order.
10. A Service Request may not be converted into multiple active Work Orders unless a later business rule explicitly supports splitting.
11. All relevant changes must be auditable.
12. No user may access a Service Request belonging to another tenant.

## 4. Work Order Lifecycle

A Work Order represents an approved piece of work that must be executed by the service organization.

A Work Order normally originates from an approved Service Request.

### 4.1 Creation

A Work Order can be created by:

- Converting an approved Service Request
- An authorized Manager/Dispatcher creating operational work directly
- Another authorized internal workflow introduced in a future version

When created from a Service Request, the Work Order maintains a reference to its originating request.

The Work Order contains the information required to execute the job, including:

- Customer
- Customer location
- Equipment/asset, when applicable
- Assigned technician
- Required skills
- Priority
- Scheduled date/time
- Work description
- Instructions
- Notes
- Attachments
- Estimated labor
- Actual labor
- Parts/materials used
- Work result
- Completion information

### 4.2 Work Order States

The initial lifecycle is:

**DRAFT → SCHEDULED → ASSIGNED → IN_PROGRESS → COMPLETED**

Additional terminal or exceptional states include:

**CANCELLED**
**ON_HOLD**
**FAILED**

Conceptually:

```text
                    ┌─────────┐
                    │  DRAFT  │
                    └────┬────┘
                         │
                         ▼
                  ┌─────────────┐
                  │  SCHEDULED  │
                  └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │   ASSIGNED  │
                  └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │ IN_PROGRESS │
                  └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │  COMPLETED  │
                  └─────────────┘


DRAFT / SCHEDULED / ASSIGNED / IN_PROGRESS
                         │
                         ▼
                     CANCELLED

SCHEDULED / ASSIGNED / IN_PROGRESS
                         │
                         ▼
                      ON_HOLD

IN_PROGRESS
     │
     ▼
   FAILED
```

The exact allowed transitions will be enforced by the backend.

### 4.3 DRAFT

`DRAFT` means the work has been created but is not yet ready for execution.

The Manager/Dispatcher may:

- Define the required work
- Set priority
- Select customer/location
- Select equipment
- Estimate required labor
- Identify required skills
- Add instructions
- Prepare the Work Order for scheduling

### 4.4 SCHEDULED

`SCHEDULED` means a time slot has been selected for the work.

Scheduling should record:

- Planned start
- Planned end
- Scheduling user
- Scheduling timestamp

The system should prevent conflicting assignments where business rules require it.

### 4.5 ASSIGNED

`ASSIGNED` means a technician has been assigned to perform the work.

The assignment should record:

- Technician
- Assigning user
- Assignment timestamp
- Relevant assignment notes

A Work Order may later support multiple technicians if the business model requires it.

### 4.6 IN_PROGRESS

`IN_PROGRESS` means the technician has started performing the work.

The system should record:

- Actual start time
- Technician
- Relevant status history

While working, the technician may record:

- Work performed
- Labor time
- Parts/materials used
- Measurements
- Equipment condition
- Notes
- Photos
- Additional problems discovered

### 4.7 ON_HOLD

`ON_HOLD` means execution cannot currently continue.

Possible reasons include:

- Waiting for a part
- Waiting for customer access
- Waiting for approval
- Safety issue
- Waiting for another technician
- External dependency

The system should require an appropriate reason when placing work on hold.

A Work Order may later return to `IN_PROGRESS`.

### 4.8 COMPLETED

`COMPLETED` means the technician has finished the operational work.

Completion should record:

- Actual completion time
- Work performed
- Labor time
- Parts/materials used
- Completion notes
- Relevant photos/documents
- Technician who completed the work

Completion does not necessarily mean that the customer has approved the work.

Customer confirmation may be a separate business step.

### 4.9 FAILED

`FAILED` means the planned work could not successfully resolve the issue or could not be completed as intended.

A failure should record:

- Failure reason
- Technician notes
- Relevant evidence
- Recommended next action

A failed Work Order may result in:

- Additional work
- A new Work Order
- Escalation
- Customer communication
- Further diagnosis

### 4.10 CANCELLED

`CANCELLED` means the Work Order will no longer be executed.

Cancellation should preserve:

- Cancellation reason
- User who cancelled it
- Timestamp

Completed Work Orders should not normally be cancellable.

### 4.11 Technician Execution

The technician workflow should be optimized around field work rather than administrative functions.

A typical technician flow is:

```text
View Assigned Job
       ↓
Review Customer / Location / Equipment
       ↓
Start Work
       ↓
Perform Work
       ↓
Record Labor / Parts / Notes / Photos
       ↓
Complete Work
       ↓
Customer Confirmation
```

The technician should not be required to manually manipulate unrelated administrative data.

### 4.12 Customer Confirmation

After operational completion, the customer may be asked to confirm the work.

Confirmation may include:

- Customer name
- Confirmation timestamp
- Digital signature or equivalent confirmation
- Optional comments

The person providing confirmation may be different from the original Requester.

The system must therefore distinguish:

**Requester ≠ Technician ≠ Approver**

unless the business rules explicitly allow the same person to occupy multiple roles.

### 4.13 Additional Problems

During execution, a technician may discover an additional problem that was not part of the original Service Request.

The technician should be able to report the additional problem without silently modifying the original request.

The additional problem may result in:

- Additional work being added to the current Work Order, if authorized
- A new Service Request
- A new Work Order
- Manager/Dispatcher review

The exact behavior will be defined by later business rules.

### 4.14 Work Order History

Important Work Order events must be historically traceable.

Examples include:

- Creation
- Status changes
- Assignment
- Reassignment
- Scheduling
- Rescheduling
- Technician start
- Technician completion
- Parts added
- Labor recorded
- Notes added
- Attachments added
- Customer confirmation
- Cancellation
- Failure
- Escalation

The system should be able to answer:

> Who performed this action, what happened, and when?

### 4.15 Business Rules

Initial rules:

1. A Work Order belongs to exactly one tenant.
2. A Work Order normally originates from one Service Request.
3. A Work Order may only be created from an approved Service Request unless an authorized direct-creation workflow is used.
4. A Work Order may have one or more assigned technicians depending on the business rules.
5. Technician assignment must respect tenant boundaries.
6. A technician may only modify Work Orders they are authorized to work on.
7. Status transitions must be validated by the backend.
8. Actual execution times must be recorded separately from scheduled times.
9. Completion must preserve the work performed and execution history.
10. Customer confirmation is separate from technician completion.
11. Completed Work Orders should be treated as historical records and should not be freely rewritten.
12. All important state changes must be auditable.
13. No user may access a Work Order belonging to another tenant.

## 5. Customers, Locations and Equipment

Customers represent organizations or individuals that receive services from a WorkFlow SaaS tenant.

The customer domain is hierarchical because a customer may have multiple physical locations, and each location may contain multiple pieces of equipment or assets.

### 5.1 Customer

A Customer belongs to exactly one Tenant.

A Customer may represent:

- A company
- An organization
- A public institution
- An individual customer

Typical customer information includes:

- Legal or display name
- Customer reference number
- Contact information
- Billing information
- Customer status
- Notes
- Customer users

The Customer is the business entity that receives the service.

A Customer must never be shared between tenants.

### 5.2 Customer Users

A Customer may have multiple users who interact with WorkFlow SaaS.

Examples include:

- Customer Administrator
- Requester
- Approver
- Other customer employees

Customer users should be associated with the Customer organization they represent.

A customer user may have access to:

- Their organization's service requests
- Their organization's work orders
- Their organization's locations
- Their organization's equipment
- Relevant service history
- Relevant invoices

Access must be restricted to the customer's own data.

A customer user must not automatically gain access to another customer belonging to the same tenant.

### 5.3 Customer Location

A Customer may have multiple physical locations.

Examples:

- Headquarters
- Warehouse
- Factory
- Office
- Retail store
- Construction site
- Branch office

A location belongs to one Customer.

Typical information includes:

- Name
- Address
- City
- Postal code
- Country
- Geographic coordinates, when required
- Contact information
- Access instructions
- Operating hours
- Status

The location is important because technicians need to know where work must be performed.

### 5.4 Location Hierarchy

The initial implementation will treat a Customer Location as a direct child of a Customer.

Future versions may support more complex structures such as:

```text
Customer
  │
  └── Site
       │
       ├── Building
       │    ├── Floor
       │    │    └── Equipment
       │    └── Equipment
       │
       └── Outdoor Area
            └── Equipment
```

This should not be implemented prematurely unless real business requirements require it.

### 5.5 Equipment / Asset

Equipment represents a physical asset that the tenant services or maintains for a customer.

Examples:

- HVAC unit
- Industrial machine
- Generator
- Boiler
- Elevator
- Refrigeration system
- Electrical equipment
- Vehicle
- Network equipment

An Equipment record belongs to one Customer Location.

Typical information includes:

- Asset name
- Asset type
- Manufacturer
- Model
- Serial number
- Asset/reference number
- Installation date
- Warranty information
- Current status
- Maintenance information
- Technical specifications

### 5.6 Equipment Status

Initial equipment statuses may include:

- ACTIVE
- INACTIVE
- UNDER_MAINTENANCE
- DECOMMISSIONED

The exact lifecycle will be refined when maintenance functionality is designed.

### 5.7 Equipment History

Equipment should have a historical record of important events.

Examples include:

- Installation
- Maintenance
- Repair
- Inspection
- Part replacement
- Status changes
- Relocation
- Warranty events
- Decommissioning

This allows the system to answer questions such as:

> What work has been performed on this machine during the last two years?

and:

> Which technicians have worked on this equipment?

### 5.8 Equipment Relocation

Equipment may move between customer locations.

The system should preserve the historical location rather than simply overwriting the old location.

For example:

```text
Equipment X

2026-01-01 → Customer Site A
2026-06-15 → Customer Site B
2027-02-10 → Customer Site C
```

The current location represents where the equipment is now, while the history preserves where it previously existed.

### 5.9 Service Request Relationship

A Service Request may reference:

- Customer
- Customer Location
- Equipment

The relationships are not necessarily all mandatory.

For example, a customer may report:

```text
"The air conditioning in Building A is not working."
```

without identifying a specific equipment record.

Another request may identify:

```text
Customer: ACME
Location: Bucharest Office
Equipment: HVAC-001
Problem: Compressor failure
```

The system should support both cases.

### 5.10 Work Order Relationship

A Work Order normally inherits or references the relevant:

- Customer
- Customer Location
- Equipment

from the originating Service Request.

The Work Order may contain more specific operational information than the original request.

### 5.11 Business Rules

Initial rules:

1. A Customer belongs to exactly one Tenant.
2. A Customer may have multiple Customer Users.
3. A Customer may have multiple Locations.
4. A Location belongs to exactly one Customer.
5. A Location may contain multiple Equipment records.
6. Equipment belongs to one Customer Location at a given point in time.
7. Equipment relocation must preserve historical location information.
8. A Service Request may reference a Customer.
9. A Service Request may optionally reference a Location.
10. A Service Request may optionally reference Equipment.
11. Equipment referenced by a Service Request must belong to the same Customer as the request.
12. A Work Order must preserve the customer and location context required to execute the job.
13. Customer data must remain isolated between tenants.
14. Customer users must only access resources belonging to their authorized Customer.
15. Historical equipment information must not be lost when current equipment attributes change.

## 6. Technicians, Skills and Scheduling

Technicians are internal tenant users who perform field-service work.

Technician management must support assigning appropriate personnel to Work Orders while considering skills, availability, workload, and scheduling constraints.

### 6.1 Technician

A Technician is an internal user who is authorized to perform field-service work.

A Technician is associated with exactly one Tenant.

The Technician may have additional information beyond the normal user account, including:

- Employee/reference number
- Contact information
- Skills
- Certifications
- Work status
- Availability
- Service area
- Current workload

The Technician's authentication identity remains the platform User.

The Technician profile represents the field-service-specific information associated with that user.

### 6.2 Technician Skills

Technicians may have multiple skills.

Examples:

- HVAC
- Electrical
- Plumbing
- Mechanical
- Refrigeration
- Network equipment
- Industrial maintenance

Skills should be modeled independently so that Work Orders can specify required skills.

Example:

```text
Technician A
    ├── HVAC
    └── Refrigeration

Technician B
    ├── Electrical
    └── Industrial Maintenance
```

A Work Order may require one or more skills.

The system can then use those requirements when assisting Managers/Dispatchers with technician assignment.

### 6.3 Certifications

Some work may require specific certifications.

Examples:

- Electrical certification
- Refrigerant handling certification
- Safety certification
- Manufacturer-specific certification

Certifications should have their own lifecycle information where necessary.

Possible information includes:

- Certification type
- Issuing organization
- Certification number
- Issue date
- Expiration date
- Status

Expired certifications must not automatically satisfy requirements for work that requires a valid certification.

### 6.4 Technician Availability

A technician's availability represents when they can normally be scheduled for work.

Availability may eventually support:

- Working days
- Working hours
- Holidays
- Time off
- Leave
- Temporary unavailability
- Emergency availability

The initial version may implement a simpler availability model and expand it later.

### 6.5 Technician Assignment

A Work Order may be assigned to one or more technicians depending on the job.

An assignment should be treated as its own business relationship rather than simply storing:

```text
work_order.technician_id
```

The assignment may contain:

- Work Order
- Technician
- Assignment role
- Assignment timestamp
- Assigning user
- Assignment status
- Notes

This allows the system to preserve assignment history.

For example:

```text
Work Order #1001

08:00 → Technician A assigned
09:15 → Technician A removed
09:20 → Technician B assigned
```

The history must not be lost.

### 6.6 Assignment Status

An assignment may have states such as:

- ASSIGNED
- ACCEPTED
- DECLINED
- REMOVED
- COMPLETED

The exact lifecycle will be refined during implementation.

A technician may optionally be required to explicitly accept an assignment.

### 6.7 Scheduling

Scheduling determines when a Work Order is expected to be performed.

Scheduling information should be separate from technician assignment.

A scheduled Work Order may contain:

- Planned start
- Planned end
- Time zone
- Scheduling user
- Scheduling timestamp
- Scheduling notes

The system must distinguish between:

**Scheduled time**

and

**Actual execution time**

For example:

```text
Scheduled:
09:00 → 11:00

Actual:
09:17 → 12:42
```

These are different business facts and must not overwrite each other.

### 6.8 Rescheduling

A Work Order may need to be rescheduled.

Reasons may include:

- Technician unavailable
- Customer unavailable
- Emergency work
- Missing parts
- Weather
- Scheduling conflict
- Customer request

Rescheduling should preserve the previous schedule.

For example:

```text
Schedule #1
2026-10-05 09:00–11:00

Schedule #2
2026-10-06 14:00–16:00
```

The current schedule represents the latest plan, while the history preserves previous plans.

### 6.9 Scheduling Conflicts

The system should detect obvious scheduling conflicts.

Examples:

```text
Technician A

10:00–12:00 → Work Order #101
11:00–13:00 → Work Order #102
```

A conflict exists because the same technician is scheduled for overlapping work.

The system should prevent or explicitly warn about such conflicts according to business rules.

### 6.10 Skills and Assignment

When assigning a technician, the system should be able to consider:

1. Required skills
2. Required certifications
3. Technician availability
4. Existing workload
5. Scheduled time
6. Service location
7. Technician service area

The initial implementation does not need to automatically optimize assignments.

A Manager/Dispatcher may make the final decision manually while the system validates constraints and provides useful information.

### 6.11 Field Service Execution

Once the scheduled Work Order begins, the Technician records actual execution information.

Typical flow:

```text
Assigned
   ↓
Scheduled
   ↓
Technician arrives
   ↓
Start Work
   ↓
Perform Work
   ↓
Record Labor / Parts / Notes
   ↓
Complete Work
```

The system should record actual start and completion times independently from scheduling.

### 6.12 Technician Workload

Managers should be able to see technician workload.

Example:

```text
Technician A

Monday:
09:00–11:00  Work Order #101
13:00–15:00  Work Order #105

Tuesday:
08:00–10:00  Work Order #109
10:30–12:30  Work Order #111
```

This information supports operational planning.

### 6.13 Business Rules

Initial rules:

1. A Technician belongs to exactly one Tenant.
2. A Technician must be represented by an authenticated User.
3. A Technician may have multiple skills.
4. A Technician may have multiple certifications.
5. Certifications may expire.
6. A Work Order may have one or more technician assignments.
7. Technician assignments must preserve history.
8. Scheduling must preserve schedule history.
9. Scheduled time and actual execution time are separate concepts.
10. The system should detect conflicting technician schedules.
11. Technician assignment must respect tenant isolation.
12. Only authorized users may assign or reassign technicians.
13. Technicians may only access Work Orders they are authorized to perform.
14. Required skills and certifications should be validated when appropriate.
15. The system must preserve enough history to determine who was assigned to a Work Order and when.

## 7. Inventory and Parts

WorkFlow SaaS supports inventory management for companies that use spare parts, materials, and consumable items during field-service operations.

Inventory is tenant-specific.

A tenant may maintain one or more inventory locations and track stock levels for items used during Work Orders.

### 7.1 Inventory Item

An Inventory Item represents a part, material, or consumable that the tenant keeps in stock.

Examples include:

- Replacement motors
- Filters
- Electrical components
- Valves
- Bearings
- Cables
- Fasteners
- Lubricants
- Safety materials

Typical information includes:

- Item name
- SKU / internal reference
- Description
- Category
- Unit of measure
- Minimum stock level
- Current status
- Optional manufacturer information
- Optional manufacturer part number

An Inventory Item belongs to exactly one Tenant.

### 7.2 Inventory Location

A tenant may maintain multiple locations where inventory is stored.

Examples:

- Main warehouse
- Technician vehicle
- Regional warehouse
- Local depot
- Service office

An Inventory Location belongs to one Tenant.

A technician vehicle may eventually be modeled as a specialized inventory location rather than as a completely separate inventory concept.

### 7.3 Stock

Stock represents the quantity of an Inventory Item currently available at an Inventory Location.

Conceptually:

```text id="6a9v7j"
Inventory Item
       │
       ├── Warehouse A → 150 units
       ├── Warehouse B → 40 units
       └── Technician Van 12 → 8 units
```

Stock should be tracked per item and location.

The system must distinguish between:

- Available quantity
- Reserved quantity
- Consumed quantity

The exact stock reservation model will be refined during implementation.

### 7.4 Stock Movements

Inventory changes must be represented as explicit stock movements rather than simply overwriting a quantity.

Examples:

- Stock received
- Stock transferred
- Stock consumed
- Stock returned
- Stock adjusted
- Stock damaged
- Stock lost

Example:

```text id="x4l5wz"
Item: FILTER-001

Warehouse A
+100  Stock received
-20   Transfer to Van 12
-5    Used on Work Order #1021
+2    Returned from Work Order #1021
```

This creates an auditable history of inventory changes.

### 7.5 Inventory Transfer

Inventory may be transferred between locations.

Example:

```text id="j1h8q4"
Warehouse A
      │
      │ 20 units
      ▼
Technician Van 12
```

A transfer should record:

- Source location
- Destination location
- Item
- Quantity
- User performing the transfer
- Timestamp
- Optional reason

The transfer must not create or destroy stock.

### 7.6 Parts Used on Work Orders

When a technician uses a part during a Work Order, the usage must be recorded explicitly.

The record should identify:

- Work Order
- Inventory Item
- Quantity
- Inventory Location
- Technician
- Timestamp
- Optional notes

For example:

```text id="6t4x4g"
Work Order #1021

FILTER-001
Quantity: 2
Used by: Technician A
Source: Van 12
```

This information can later be used for:

- Customer billing
- Cost calculation
- Inventory reporting
- Technician activity
- Equipment maintenance history

### 7.7 Returning Unused Parts

A technician may take parts to a job but not use all of them.

The system should support returning unused parts to inventory.

Example:

```text id="0v5k9s"
Issued:
5 filters

Used:
3 filters

Returned:
2 filters
```

The inventory history should clearly distinguish between issued, consumed, and returned quantities.

### 7.8 Stock Adjustments

Authorized users may need to correct inventory discrepancies.

Examples:

- Damaged stock
- Lost stock
- Incorrect previous count
- Physical inventory correction

Adjustments must require:

- Quantity
- Reason
- User
- Timestamp

Stock adjustments should be auditable.

### 7.9 Low Stock

An Inventory Item may define a minimum stock threshold.

Example:

```text id="rj3d4z"
Minimum stock: 20

Current stock: 15
```

The system may generate a low-stock event or notification.

Future versions may support:

- Automatic notifications
- Purchase recommendations
- Supplier management
- Purchase orders

These are outside the initial scope.

### 7.10 Inventory and Tenant Isolation

Inventory is strictly tenant-specific.

A tenant must never be able to:

- View another tenant's inventory
- Transfer another tenant's stock
- Consume another tenant's stock
- Modify another tenant's stock
- Infer another tenant's inventory quantities

Inventory cache keys, background jobs, reports, events, and notifications must also respect tenant boundaries.

### 7.11 Inventory and Work Order Completion

A Work Order may contain multiple consumed parts.

Example:

```text id="z7y2as"
Work Order #1021

Parts:
    FILTER-001 × 2
    SEAL-014   × 1
    CABLE-003  × 5
```

The Work Order should preserve the parts used even after inventory quantities change.

This allows the system to answer:

> Which parts were used to complete this job?

and:

> How much did the consumed material cost?

### 7.12 Business Rules

Initial rules:

1. Inventory belongs to exactly one Tenant.
2. Inventory Items belong to exactly one Tenant.
3. Inventory Locations belong to exactly one Tenant.
4. Stock is tracked per Inventory Item and Inventory Location.
5. Stock changes must be represented by auditable movements.
6. Transfers must identify source and destination locations.
7. Parts consumed during a Work Order must be recorded explicitly.
8. Parts used on a Work Order must belong to the same tenant as the Work Order.
9. Inventory quantities must not become negative unless a future business rule explicitly permits negative stock.
10. Authorized users may perform stock adjustments.
11. Stock adjustments require a reason.
12. Unused parts may be returned to inventory.
13. Work Order part usage must remain historically available after the stock quantity changes.
14. Low-stock conditions may generate notifications.
15. Inventory operations must enforce tenant isolation.

## 8. Maintenance Plans and Preventive Maintenance

WorkFlow SaaS supports preventive maintenance for equipment that requires recurring inspections, servicing, or scheduled maintenance.

Preventive maintenance differs from reactive service:

**Reactive:** Customer reports a problem → Service Request → Work Order

**Preventive:** Maintenance Plan becomes due → Work Order

### 8.1 Maintenance Plan

A Maintenance Plan defines recurring maintenance requirements for equipment or a group of equipment.

A plan may define:

- Customer
- Customer location
- Equipment
- Maintenance type
- Description
- Required skills
- Required certifications
- Recurrence
- Next due date
- Preferred scheduling window
- Estimated duration
- Instructions
- Required parts/materials
- Plan status

A Maintenance Plan belongs to exactly one Tenant.

### 8.2 Maintenance Types

Examples include:

- Inspection
- Preventive maintenance
- Calibration
- Cleaning
- Safety check
- Filter replacement
- Lubrication
- Regulatory inspection

The maintenance type should describe the nature of the recurring activity.

### 8.3 Recurrence

A maintenance plan may recur according to a schedule.

Examples:

```text
Every 30 days
Every 3 months
Every 6 months
Every 12 months
```

Future versions may support more advanced recurrence rules.

The system must distinguish between:

- Last completed maintenance
- Current due date
- Next scheduled maintenance

These are different business facts.

### 8.4 Maintenance Due

When a maintenance plan approaches or reaches its due date, the system should identify it as requiring action.

Possible states include:

- UPCOMING
- DUE
- OVERDUE
- SCHEDULED
- COMPLETED
- SKIPPED
- CANCELLED

The exact state model will be refined during implementation.

### 8.5 Automatic Work Order Generation

A maintenance plan may generate a Work Order when maintenance becomes due.

Example:

```text
Maintenance Plan
    │
    │ Every 6 months
    ▼
Maintenance becomes due
    │
    ▼
Work Order generated
    │
    ▼
Technician assigned
    │
    ▼
Maintenance performed
    │
    ▼
Work Order completed
    │
    ▼
Next maintenance date calculated
```

The generated Work Order should identify the Maintenance Plan that caused it to be created.

This allows the system to answer:

> Why was this Work Order created?

### 8.6 Maintenance History

Completed preventive maintenance must become part of the equipment's service history.

For example:

```text
Equipment: HVAC-001

2026-01-15
Annual inspection completed

2026-07-15
Filter replacement completed

2027-01-15
Annual inspection completed
```

The system should preserve the historical Work Orders rather than overwriting previous maintenance records.

### 8.7 Maintenance Exceptions

Preventive maintenance may not always occur exactly on the planned date.

Possible situations include:

- Customer unavailable
- Technician unavailable
- Missing parts
- Equipment inaccessible
- Emergency work taking priority
- Maintenance intentionally postponed

The system should allow authorized users to reschedule or skip a maintenance occurrence while preserving the reason.

### 8.8 Maintenance and Equipment

A Maintenance Plan may apply to:

- One specific Equipment record
- Multiple similar equipment records, in a future version

The initial implementation should focus on plans associated with individual equipment where practical.

### 8.9 Maintenance and Inventory

A maintenance plan may define expected parts or materials.

Example:

```text
HVAC Annual Maintenance

Required:
    Air Filter × 2
    Lubricant × 1
```

The generated Work Order can use these requirements as a starting point.

Actual consumption must still be recorded based on what the technician actually uses.

### 8.10 Maintenance and Technician Skills

A maintenance plan may define required skills or certifications.

For example:

```text
Maintenance:
Industrial Safety Inspection

Required:
    Skill: Industrial Maintenance
    Certification: Safety Inspection
```

The assignment process should validate these requirements.

### 8.11 Notifications

The system may notify relevant users when maintenance is:

- Approaching
- Due
- Overdue
- Scheduled
- Completed

Possible recipients include:

- Manager/Dispatcher
- Tenant Administrator
- Customer Administrator
- Customer Approver

Notification rules will be defined separately.

### 8.12 Business Rules

Initial rules:

1. A Maintenance Plan belongs to exactly one Tenant.
2. A Maintenance Plan may be associated with one or more equipment records depending on the implementation.
3. A maintenance plan defines recurring maintenance requirements.
4. The system must track the last completed maintenance and next due date.
5. Due and overdue maintenance must be identifiable.
6. A Maintenance Plan may generate a Work Order.
7. Generated Work Orders must retain a reference to their Maintenance Plan.
8. Completed maintenance must remain part of equipment history.
9. Rescheduled or skipped maintenance must preserve the reason and history.
10. Required skills and certifications may be defined for maintenance work.
11. Required parts may be defined as expected materials, but actual consumption must be recorded independently.
12. Maintenance processing must respect tenant isolation.
13. Background maintenance jobs must process tenant-specific data within the correct tenant context.
14. Maintenance notifications must never cross tenant boundaries.

## 9. Users, Authentication and Authorization

WorkFlow SaaS requires authenticated users and a structured authorization model.

Identity, roles, permissions, and tenant membership must be treated as separate concepts.

### 9.1 User

A User represents an authenticated person who can access the platform.

Typical information includes:

- Unique identifier
- Email address
- Password hash
- First name
- Last name
- Phone number
- Account status
- Creation timestamp
- Last login timestamp

Passwords must never be stored in plain text.

The backend must store only a secure password hash using an appropriate password-hashing algorithm.

### 9.2 User Account Status

Initial account states may include:

- ACTIVE
- INVITED
- LOCKED
- DISABLED

A disabled or locked user must not be able to authenticate normally.

### 9.3 Tenant Membership

A User's identity is separate from their membership in a Tenant.

This allows the system to represent:

```text
User
  │
  ├── Tenant A → Manager
  │
  └── Tenant B → Technician
```

if the business rules eventually allow a person to belong to multiple tenants.

The tenant membership should therefore contain information such as:

- User
- Tenant
- Membership status
- Roles
- Membership creation timestamp

The initial implementation may restrict users to one tenant if that simplifies the first release, but the domain should not unnecessarily couple global identity with tenant-specific authorization.

### 9.4 Roles

Roles represent collections of permissions.

Initial roles include:

- Platform Administrator
- Tenant Administrator
- Manager / Dispatcher
- Technician
- Customer Administrator
- Requester
- Approver

A tenant may configure which roles are assigned to its users according to its subscription and authorization model.

### 9.5 Permissions

Permissions represent individual capabilities.

Examples:

```text
USER_VIEW
USER_MANAGE

CUSTOMER_VIEW
CUSTOMER_MANAGE

EQUIPMENT_VIEW
EQUIPMENT_MANAGE

SERVICE_REQUEST_CREATE
SERVICE_REQUEST_VIEW
SERVICE_REQUEST_UPDATE
SERVICE_REQUEST_REVIEW
SERVICE_REQUEST_APPROVE
SERVICE_REQUEST_REJECT

WORK_ORDER_VIEW
WORK_ORDER_CREATE
WORK_ORDER_ASSIGN
WORK_ORDER_SCHEDULE
WORK_ORDER_START
WORK_ORDER_COMPLETE
WORK_ORDER_CANCEL

INVENTORY_VIEW
INVENTORY_MANAGE

MAINTENANCE_VIEW
MAINTENANCE_MANAGE

INVOICE_VIEW
INVOICE_MANAGE

REPORT_VIEW
```

The final permission catalog will be defined during the authorization design.

### 9.6 Role-to-Permission Relationship

A role may contain multiple permissions.

For example:

```text
Manager / Dispatcher
    │
    ├── SERVICE_REQUEST_VIEW
    ├── SERVICE_REQUEST_REVIEW
    ├── SERVICE_REQUEST_APPROVE
    ├── WORK_ORDER_VIEW
    ├── WORK_ORDER_CREATE
    ├── WORK_ORDER_ASSIGN
    ├── WORK_ORDER_SCHEDULE
    └── REPORT_VIEW
```

This allows authorization to be expressed in terms of capabilities rather than hard-coded role names.

### 9.7 Authentication

The initial backend authentication mechanism will use:

- Spring Security
- JWT-based authentication
- Secure password hashing

After successful authentication, the backend issues an access token representing the authenticated identity.

The token may contain claims such as:

- User ID
- Tenant context
- Roles
- Relevant authorization information
- Token expiration

Sensitive business data should not be placed into the token unnecessarily.

### 9.8 Authorization

Authorization must occur on the backend.

The frontend may hide functionality that the user cannot access, but the backend must always enforce the actual security rules.

For example, hiding:

```text
Delete Customer
```

in Angular is not a security control.

The backend must reject an unauthorized request even if the user manually calls the API.

### 9.9 Tenant Context

Every tenant-scoped request must execute within an explicit tenant context.

Conceptually:

```text
Authenticated User
        │
        ▼
Tenant Context
        │
        ▼
Authorization
        │
        ▼
Tenant-scoped Business Operation
```

The tenant context must not be trusted solely because the client sends a tenant ID in the request body or URL.

The backend must derive the authorized tenant context from the authenticated identity and membership.

### 9.10 Tenant Isolation

Tenant isolation must be enforced at multiple levels.

For example:

```text
Request
   ↓
Authentication
   ↓
Identify User
   ↓
Identify Authorized Tenant
   ↓
Authorization
   ↓
Tenant-scoped Query
   ↓
Business Operation
```

A request such as:

```text
GET /api/customers/123
```

must not return Customer `123` simply because that ID exists.

The backend must verify that the customer belongs to a tenant the authenticated user is authorized to access.

### 9.11 Cross-Tenant Access

Cross-tenant access is considered a critical security violation.

The system must prevent:

- Reading another tenant's records
- Updating another tenant's records
- Deleting another tenant's records
- Searching another tenant's records
- Accessing another tenant's files
- Receiving another tenant's notifications
- Accessing another tenant's cached information
- Receiving another tenant's events
- Viewing another tenant's reports

Tenant isolation must therefore be treated as an architectural property rather than a controller-level check.

### 9.12 Platform Administrator

Platform Administrators operate at a different authorization scope.

They may need platform-level access to:

- Tenants
- Subscriptions
- Platform configuration
- Platform health
- Security/audit information

Platform-level access must be explicitly modeled.

A Platform Administrator should not automatically inherit every tenant role.

Likewise, a Tenant Administrator must not gain platform-level privileges merely because they have administrative permissions inside their tenant.

### 9.13 Customer User Isolation

Customer users have an additional authorization boundary.

For example:

```text
Tenant A
   │
   ├── Customer X
   │      ├── User X1
   │      └── User X2
   │
   └── Customer Y
          ├── User Y1
          └── User Y2
```

User X1 must not automatically access Customer Y's information.

Therefore authorization may need to evaluate:

- Tenant
- Customer
- Resource
- Permission
- User relationship

### 9.14 Authentication Sessions and Token Expiration

Access tokens must have a limited lifetime.

The authentication design may later include:

- Access tokens
- Refresh tokens
- Token rotation
- Revocation
- Logout/session invalidation
- Device/session tracking

The exact mechanism will be defined during the security implementation phase.

### 9.15 Auditability

Security-sensitive actions should be auditable.

Examples:

- Login
- Failed login
- Password change
- Role assignment
- Permission changes
- User activation/deactivation
- Tenant membership changes
- Sensitive resource access
- Administrative actions

The audit system must preserve the tenant context where applicable.

### 9.16 Business Rules

Initial rules:

1. A User represents an authenticated identity.
2. A User must never have a plaintext password stored.
3. Tenant membership is separate from global user identity.
4. Roles contain permissions.
5. Permissions represent specific capabilities.
6. Authorization is enforced by the backend.
7. Tenant context must be derived from authenticated identity and authorization.
8. Client-provided tenant IDs must not be trusted as the sole authorization mechanism.
9. Every tenant-scoped resource access must enforce tenant isolation.
10. Customer users must be restricted to their authorized customer organization.
11. Platform Administrators and Tenant Administrators have different authorization scopes.
12. Access tokens must expire.
13. Security-sensitive operations must be auditable.
14. Cross-tenant access must be explicitly tested and treated as a critical security failure.

## 10. Subscriptions and Invoicing

WorkFlow SaaS contains two distinct financial domains:

1. SaaS subscription billing between WorkFlow SaaS and its tenants.
2. Service invoicing between tenants and their customers.

These domains must remain separate.

---

### 10.1 SaaS Subscription

A Subscription represents a tenant's commercial relationship with WorkFlow SaaS.

A subscription belongs to exactly one Tenant.

A subscription references a Subscription Plan.

Initial plans:

- Starter
- Professional
- Enterprise

The exact pricing and feature limits will be defined separately.

### 10.2 Subscription Lifecycle

A subscription may have the following states:

```text
TRIAL
  ↓
ACTIVE
  ↓
PAST_DUE
  ↓
SUSPENDED
  ↓
CANCELLED
```

Other transitions may be introduced when payment and billing requirements are implemented.

The backend must enforce the consequences of each subscription state.

For example:

- `TRIAL` → normal trial access
- `ACTIVE` → normal subscribed access
- `PAST_DUE` → payment requires attention
- `SUSPENDED` → restricted platform access
- `CANCELLED` → subscription terminated

The exact access rules will be defined during implementation.

### 10.3 Subscription Plan

A Subscription Plan defines the capabilities and limits available to a tenant.

Possible limits include:

- Number of users
- Number of customers
- Number of active technicians
- Number of equipment records
- Storage capacity
- Monthly Work Orders
- API usage
- Reporting capabilities

Plans may also enable or disable specific features.

For example:

```text
Professional
    ├── Advanced Reporting
    ├── Inventory
    ├── Maintenance Plans
    └── API Access
```

Feature availability must be enforced by the backend.

The frontend may hide unavailable functionality, but frontend restrictions are not sufficient.

### 10.4 Subscription Usage

The platform may track tenant usage against plan limits.

Examples:

```text
Users:
18 / 25

Customers:
143 / 500

Storage:
18 GB / 50 GB
```

Usage calculations must always be scoped to the correct tenant.

A tenant must never be able to influence or access another tenant's usage information.

### 10.5 Payment Provider

A future version will integrate with an external payment provider such as Stripe.

The payment provider may handle:

- Payment methods
- Recurring payments
- Invoices for SaaS subscriptions
- Payment retries
- Payment failures
- Subscription lifecycle events

The platform should not store sensitive payment-card information.

Instead, the application should store references and relevant billing metadata provided by the payment provider.

### 10.6 Payment Webhooks

Subscription state changes may be received through payment-provider webhooks.

Examples include:

- Subscription created
- Subscription renewed
- Payment succeeded
- Payment failed
- Subscription cancelled

Webhook processing must be:

- Authenticated
- Idempotent
- Auditable
- Safe to retry

A webhook must not be able to modify an unrelated tenant's subscription.

### 10.7 Customer Invoice

A Customer Invoice represents a financial charge issued by a tenant to one of its customers.

A Customer Invoice may be generated from completed service work.

For example:

```text
Work Order #1021
    │
    ├── Labor
    ├── Parts
    └── Additional Charges
          │
          ▼
       Invoice
```

The invoice belongs to the Tenant and references the Customer.

### 10.8 Invoice Contents

An invoice may contain:

- Invoice number
- Customer
- Billing address
- Invoice date
- Due date
- Currency
- Line items
- Labor charges
- Parts/material charges
- Discounts
- Taxes
- Subtotal
- Total
- Payment status
- Notes

Invoice line items should preserve the information necessary to explain how the amount was calculated.

### 10.9 Invoice Line Items

Possible line items include:

```text
Labor
    3 hours × €60 = €180

Parts
    Filter × 2 = €40

Travel
    1 × €25 = €25
```

An invoice line should preserve the applicable price at the time of invoicing.

Historical invoices must not change simply because a product or service price changes later.

### 10.10 Invoice Status

Initial invoice states may include:

- DRAFT
- ISSUED
- PARTIALLY_PAID
- PAID
- OVERDUE
- CANCELLED

The exact transition rules will be defined during the financial implementation.

### 10.11 Invoice and Work Order

A completed Work Order may contribute billable items to an invoice.

The system should preserve the relationship between:

```text
Work Order
     ↓
Billable Items
     ↓
Invoice
     ↓
Invoice Lines
```

However, an invoice should preserve its own financial snapshot.

If a Work Order is later modified, an already issued invoice must not silently change.

### 10.12 Customer Payments

Future versions may support recording customer payments.

Possible information includes:

- Payment date
- Amount
- Currency
- Payment method
- Reference
- Invoice
- Payment status

The initial MVP may support invoice creation and status tracking without implementing a complete payment collection system.

### 10.13 Currency

Invoices must specify their currency.

A tenant may eventually operate with customers in different currencies.

Currency should therefore be stored explicitly rather than inferred from locale.

### 10.14 SaaS Billing vs Customer Billing

The two billing domains must never be confused.

```text
                    WorkFlow SaaS
                         │
                         │ subscription payment
                         ▼
                       Tenant
                         │
                         │ service invoice
                         ▼
                      Customer
```

The SaaS subscription determines whether a tenant may use platform functionality.

Customer invoices represent money owed to the tenant for services provided.

These are separate entities, workflows, permissions, and financial records.

### 10.15 Business Rules

Initial rules:

1. A Subscription belongs to exactly one Tenant.
2. A Subscription references one Subscription Plan.
3. Subscription lifecycle changes must be validated.
4. Subscription plans may define feature and usage limits.
5. Subscription limits must be enforced by the backend.
6. Payment-provider webhooks must be authenticated and idempotent.
7. SaaS billing data must be isolated between tenants.
8. A Customer Invoice belongs to exactly one Tenant.
9. A Customer Invoice references a Customer.
10. Invoice line items preserve their historical financial values.
11. Issued invoices must not silently change when source prices change.
12. Completed Work Orders may contribute billable items to invoices.
13. SaaS subscription invoices and tenant customer invoices are separate financial domains.
14. Sensitive payment-card data must not be stored by WorkFlow SaaS.
15. Financial operations must be auditable.
16. All financial data must respect tenant isolation.

## 11. Notifications, Documents and Audit Events

Notifications, documents, and audit events are cross-cutting capabilities used by multiple WorkFlow SaaS modules.

They must be designed as separate domains even though they frequently interact with the same business operations.

---

### 11.1 Notifications

A Notification informs a user that an event occurred or that an action requires their attention.

Notifications may be triggered by:

- New Service Request
- Service Request status change
- Work Order assignment
- Work Order scheduling
- Work Order rescheduling
- Work Order completion
- Work Order failure
- Maintenance becoming due
- Maintenance becoming overdue
- Invoice issued
- Invoice becoming overdue
- Subscription payment failure
- User invitation
- Approval request
- Escalation
- System/security event

A notification should contain enough information for the recipient to understand what happened and what action may be required.

### 11.2 Notification Channels

The initial notification system may support:

- In-app notifications
- Email

Future channels may include:

- SMS
- Push notifications
- WebSocket/real-time notifications

Different notification types may support different channels.

For example:

```text id="v7jq2h"
Work Order Assigned
       │
       ├── In-app notification
       └── Email
```

### 11.3 Notification Structure

A notification may contain:

- Recipient
- Tenant
- Notification type
- Title
- Message
- Related entity
- Related entity ID
- Read/unread state
- Created timestamp
- Delivery status
- Delivery timestamp

The related entity may reference a:

- Service Request
- Work Order
- Maintenance Plan
- Invoice
- User
- Subscription
- Other supported business entity

### 11.4 Notification Preferences

Users may eventually configure how they receive notifications.

Possible preferences include:

- Work Order assignments
- Schedule changes
- Service Request updates
- Maintenance reminders
- Invoice events
- System notifications
- Email enabled/disabled
- In-app enabled/disabled

Tenant-level configuration may also control certain notification behavior.

Mandatory security notifications should not necessarily be disableable.

### 11.5 Notification Delivery

Notification generation should be separated from notification delivery.

For example:

```text id="r3t1mm"
Business Event
      ↓
Notification Created
      ↓
Delivery Process
      ├── In-App
      └── Email
```

This allows the application to continue operating even when an external email provider is temporarily unavailable.

Long-running or external notification delivery may later use asynchronous processing through Kafka.

### 11.6 Notification Reliability

Notification delivery should account for:

- Temporary provider failures
- Retries
- Duplicate events
- Invalid recipients
- Rate limits
- Delivery failures

The system should avoid creating duplicate notifications when the same business event is processed more than once.

Notification processing should therefore support idempotency where appropriate.

### 11.7 Tenant Isolation

Notifications are tenant-scoped.

A user must never receive a notification containing information from another tenant.

Tenant isolation applies to:

- Notification creation
- Notification retrieval
- Notification delivery
- Notification preferences
- Background processing
- Event consumption
- Reporting

Background jobs must preserve the correct tenant context.

---

## 11.8 Documents and Files

Documents represent files associated with business entities.

Examples include:

- Equipment manuals
- Service reports
- Technician photos
- Customer attachments
- Work Order photos
- Inspection reports
- Invoices
- Certificates
- Maintenance documentation

The application should store file metadata in the database while storing the actual file content in object storage.

AWS S3 is the planned object-storage solution.

Conceptually:

```text id="6r2w1x"
Application
     │
     ├── File Metadata → PostgreSQL
     │
     └── File Content → Object Storage
```

The database should not be used as the primary storage location for large files.

### 11.9 File Metadata

A file record may contain:

- File ID
- Tenant
- Original filename
- Storage key
- Content type
- File size
- Uploading user
- Upload timestamp
- Related entity
- Related entity ID
- File status

Additional metadata may include:

- Checksum
- Description
- Visibility
- Version
- Uploaded source

### 11.10 File Relationships

Files may be attached to supported business entities.

Examples:

```text id="3s5gye"
Service Request
    └── Customer photo

Work Order
    ├── Before photo
    ├── During-work photo
    ├── After photo
    └── Service report

Equipment
    ├── Manual
    ├── Certificate
    └── Inspection report
```

The exact attachment model will be defined during database design.

### 11.11 File Access Control

File access must follow the permissions of the entity to which the file belongs.

A user who cannot access a Work Order must not be able to access files attached to that Work Order.

File URLs must not provide a way to bypass application authorization.

Object storage access should use controlled access mechanisms such as short-lived signed URLs where appropriate.

### 11.12 File Security

Uploaded files should be validated.

Validation may include:

- File size limits
- Allowed content types
- Filename validation
- Malware/virus scanning where appropriate
- Storage isolation
- Access authorization

The system must not blindly trust the filename or client-provided MIME type.

Sensitive files must not become publicly accessible by default.

### 11.13 File Lifecycle

Files may have a lifecycle such as:

```text id="r1q0by"
UPLOADED
   ↓
AVAILABLE
   ↓
ARCHIVED
   ↓
DELETED
```

Deletion behavior must account for audit requirements and business retention rules.

Financial, compliance, or audit-related documents may require retention even after their originating business entity changes state.

---

## 11.14 Audit Events

Audit Events provide a historical record of important system actions.

Audit logging is different from ordinary application logging.

Application logs help developers and operators understand what the system is doing.

Audit events provide a business and security history of **who did what, when, and to which resource**.

### 11.15 Auditable Actions

Examples include:

- User login
- Failed login
- Password change
- User creation
- User disabling
- Role assignment
- Permission changes
- Tenant membership changes
- Customer creation
- Equipment changes
- Service Request status changes
- Work Order assignment
- Work Order status changes
- Schedule changes
- Inventory adjustments
- Invoice issuance
- Invoice cancellation
- Subscription changes
- File access
- Administrative actions

Not every low-level technical operation needs to create an audit event.

Audit events should focus on meaningful business and security actions.

### 11.16 Audit Event Structure

An Audit Event may contain:

- Event ID
- Tenant
- Actor/user
- Action
- Entity type
- Entity ID
- Timestamp
- Result
- Source
- IP address where appropriate
- Request/correlation ID
- Relevant before/after information where appropriate

Example:

```text id="f8r2c4"
Actor: user-123
Action: WORK_ORDER_STATUS_CHANGED
Entity: WorkOrder
Entity ID: wo-456
Previous: IN_PROGRESS
New: COMPLETED
Timestamp: 2026-09-24T10:30:00
```

### 11.17 Audit Immutability

Audit records should be treated as historical records.

Normal application users must not be able to modify or delete audit events.

If an audit record contains incorrect information, the system should record a corrective event rather than silently rewriting history.

Access to audit data should itself be controlled and, where appropriate, audited.

### 11.18 Audit and Tenant Isolation

Audit events must respect tenant boundaries.

A Tenant Administrator may view audit events belonging to their tenant according to their permissions.

A user from Tenant A must never access audit events belonging to Tenant B.

Platform administrators may have separate platform-level audit access.

The authorization model must clearly distinguish:

```text id="u2n4k8"
Platform Audit
       │
       └── Platform-level actions

Tenant Audit
       │
       └── Tenant-specific actions
```

### 11.19 Correlation and Traceability

Important business operations should be traceable across application components.

For example:

```text id="p4x7d2"
API Request
    ↓
Work Order Updated
    ↓
Audit Event
    ↓
Business Event
    ↓
Notification
    ↓
Email Delivery
```

A correlation ID should be used where appropriate so that related operations can be traced across logs, asynchronous processing, and external integrations.

### 11.20 Business Rules

Initial rules:

1. Notifications and audit events are separate concepts.
2. Notifications are intended for user communication and action.
3. Audit events provide historical records of important business and security actions.
4. Notifications must respect tenant isolation.
5. Background notification processing must preserve tenant context.
6. Duplicate notification delivery should be prevented where idempotency is required.
7. File metadata is stored in PostgreSQL.
8. File content is stored in object storage.
9. File access must follow the authorization rules of the related entity.
10. Users must not bypass application authorization through object-storage URLs.
11. Uploaded files must be subject to appropriate validation and security controls.
12. Sensitive files must not be publicly accessible by default.
13. Audit events should record meaningful business and security actions.
14. Audit records should be treated as immutable historical records.
15. Normal users must not modify or delete audit events.
16. Platform-level and tenant-level audit access must remain separate.
17. Audit data must respect tenant isolation.
18. Important asynchronous operations should support correlation and traceability.
19. Financial, security, and compliance-related records may require retention beyond the lifecycle of their originating entity.
20. Cross-tenant access to notifications, files, or audit events must be explicitly prevented and tested.

## 12. Dashboards, Reporting and Search

Dashboards, reporting, and search provide users with visibility into the operational and business data available to them.

The information presented must respect the user's permissions, tenant boundaries, and customer boundaries.

---

### 12.1 Role-Oriented Dashboards

WorkFlow SaaS should provide dashboards based on the user's role and responsibilities.

The system should not expose every available metric to every user.

Examples:

```text
Tenant Administrator
    ├── Users
    ├── Customers
    ├── Work Orders
    ├── Service Requests
    ├── Equipment
    ├── Subscription
    └── Usage

Manager / Dispatcher
    ├── New Requests
    ├── Unassigned Work
    ├── Today's Schedule
    ├── Technician Workload
    ├── Overdue Work
    └── Escalations

Technician
    ├── Today's Jobs
    ├── Upcoming Jobs
    ├── In-Progress Work
    ├── Completed Work
    └── Required Actions

Customer Administrator
    ├── Open Requests
    ├── Active Work Orders
    ├── Equipment
    ├── Recent Service
    └── Invoices
```

The exact dashboard layout will be determined during frontend design.

---

### 12.2 Tenant Administrator Dashboard

A Tenant Administrator may see high-level operational and account information.

Possible metrics include:

- Active users
- Active technicians
- Customers
- Equipment
- Open Service Requests
- Open Work Orders
- Overdue Work Orders
- Completed Work Orders
- Upcoming Maintenance
- Overdue Maintenance
- Outstanding invoices
- Subscription status
- Plan usage

Dashboard information must only represent the administrator's tenant.

---

### 12.3 Manager / Dispatcher Dashboard

The Manager / Dispatcher dashboard focuses on daily operations.

Possible information includes:

- New Service Requests
- Requests awaiting review
- High-priority requests
- Unassigned Work Orders
- Today's scheduled Work Orders
- Technician workload
- Overlapping schedules
- Overdue Work Orders
- Work Orders on hold
- Failed Work Orders
- Escalations
- Upcoming maintenance

The dashboard should help identify work requiring attention rather than simply display raw data.

---

### 12.4 Technician Dashboard

The Technician dashboard focuses on field work.

Possible information includes:

- Today's assigned Work Orders
- Upcoming Work Orders
- Work Orders in progress
- Work Orders on hold
- Recently completed Work Orders
- Required skills/certifications
- Schedule
- Important notifications

A technician should be able to quickly identify:

1. Where they need to go.
2. What work needs to be performed.
3. When it is scheduled.
4. What equipment is involved.
5. What instructions or requirements apply.

---

### 12.5 Customer Dashboard

Customer users should only see information belonging to their customer organization.

Possible information includes:

- Open Service Requests
- Request statuses
- Active Work Orders
- Scheduled service
- Recent completed work
- Equipment
- Maintenance history
- Invoices
- Notifications
- Documents available to the customer

Customer users must not see internal tenant information that is not intended for them.

For example, internal technician notes, internal scheduling notes, or internal operational comments may require restricted visibility.

---

### 12.6 Operational Metrics

The platform may calculate operational metrics such as:

- Open Work Orders
- Average completion time
- Work Orders completed per period
- Overdue Work Orders
- Technician workload
- Work Order status distribution
- Service Request volume
- Service Request response time
- Maintenance completion rate
- Failed Work Orders
- Customer confirmation rate

Metrics must have clearly defined calculation rules.

For example:

```text
Average Completion Time

= completion timestamp
  - actual start timestamp
```

The definition of each metric should remain consistent across dashboards and reports.

---

### 12.7 Reporting

Reporting allows authorized users to analyze historical business data.

Initial report categories may include:

#### Service Requests

- Requests by status
- Requests by priority
- Requests by customer
- Requests by equipment
- Requests over time

#### Work Orders

- Work Orders by status
- Work Orders by technician
- Work Orders by customer
- Work Orders by equipment
- Completion times
- Overdue Work Orders
- Failed Work Orders

#### Maintenance

- Upcoming maintenance
- Overdue maintenance
- Completed maintenance
- Maintenance history
- Equipment maintenance frequency

#### Inventory

- Current stock
- Low-stock items
- Stock movements
- Parts consumed
- Parts used by Work Order

#### Financial

- Issued invoices
- Paid invoices
- Outstanding invoices
- Overdue invoices
- Revenue-related reporting

Financial reports must be restricted to appropriately authorized users.

---

### 12.8 Reporting Time Periods

Reports should support meaningful time ranges.

Examples:

- Today
- Yesterday
- Current week
- Previous week
- Current month
- Previous month
- Current quarter
- Previous quarter
- Current year
- Custom date range

The application should define whether timestamps are interpreted according to tenant, user, customer, or system timezone where appropriate.

---

### 12.9 Search

The platform should provide search across the entities that users are authorized to access.

Possible searchable entities include:

- Customers
- Customer locations
- Equipment
- Service Requests
- Work Orders
- Technicians
- Invoices
- Maintenance Plans
- Inventory Items
- Users

Search results must be tenant-scoped.

A search request must never return an entity that the requesting user is not authorized to access.

---

### 12.10 Search Criteria

Search may support:

- Exact identifiers
- Names
- Email addresses
- Phone numbers
- Equipment references
- Serial numbers
- Invoice numbers
- Work Order numbers
- Service Request numbers
- SKU/reference numbers
- Status
- Priority
- Date ranges

The exact search capabilities will depend on the database and search requirements.

---

### 12.11 Filtering and Sorting

List views should support appropriate filtering and sorting.

Examples:

```text
Work Orders

Status: IN_PROGRESS
Priority: HIGH
Technician: David
Date: This Week
Sort: Scheduled Start
```

Filtering must be performed server-side for protected business data.

The backend must never return unauthorized records simply because a client applied a filter incorrectly.

---

### 12.12 Pagination

Large datasets must not be returned in a single API response.

Endpoints returning collections should support pagination.

Examples include:

- Customers
- Work Orders
- Service Requests
- Audit Events
- Notifications
- Inventory
- Invoices

The API should define consistent pagination behavior.

The exact pagination strategy may be offset-based initially and may later use cursor-based pagination where appropriate.

---

### 12.13 Dashboard Performance

Dashboards may require data from multiple business domains.

For example:

```text
Dashboard
    ├── Work Orders
    ├── Service Requests
    ├── Technicians
    ├── Maintenance
    └── Invoices
```

Dashboard queries should not unnecessarily load large datasets into application memory.

Aggregations should be performed efficiently at the database or appropriate reporting layer.

Frequently requested data may later be cached with Redis.

Caching must always preserve tenant and authorization boundaries.

---

### 12.14 Reporting and Asynchronous Processing

Some reports may become expensive as the amount of tenant data grows.

Large reports may eventually be generated asynchronously.

Conceptually:

```text
User
  ↓
Request Report
  ↓
Background Job
  ↓
Generate Report
  ↓
Store Result
  ↓
Notify User
  ↓
Download Report
```

Kafka may eventually be used for suitable asynchronous workflows.

The initial implementation should not introduce Kafka solely because reporting exists.

---

### 12.15 Export

Authorized users may eventually export data.

Possible formats:

- CSV
- Excel
- PDF

Exports must use the same authorization and tenant-isolation rules as normal API responses.

An export must never become a mechanism for bypassing access restrictions.

Large exports may be processed asynchronously.

---

### 12.16 Dashboard and Reporting Authorization

Dashboard and reporting access must be controlled by permissions.

Examples:

```text
REPORT_VIEW
REPORT_EXPORT
FINANCIAL_REPORT_VIEW
AUDIT_VIEW
```

A user may have permission to view operational reports without having permission to view financial information.

Permissions should therefore be granular enough to separate sensitive business domains.

---

### 12.17 Business Rules

Initial rules:

1. Dashboards are role-oriented.
2. Dashboard data must respect user permissions.
3. Dashboard data must respect tenant isolation.
4. Customer users must only access information belonging to their customer organization.
5. Internal tenant information must not automatically become visible to customers.
6. Reports must use clearly defined and consistent calculation rules.
7. Financial reports require appropriate authorization.
8. Search results must be authorization-aware.
9. Filtering and sorting must not bypass authorization.
10. Large collections must support pagination.
11. Dashboard queries must be designed to avoid unnecessary large data loads.
12. Redis caching may be introduced for frequently requested dashboard data.
13. Cached data must preserve tenant and authorization boundaries.
14. Large reports and exports may be processed asynchronously.
15. Exported data must obey the same authorization rules as normal API responses.
16. Large exports must not be used to bypass tenant or permission restrictions.
17. Cross-tenant dashboard, search, reporting, and export access must be explicitly prevented and tested.

## 13. API Design and Backend Communication

The WorkFlow SaaS backend exposes a REST API consumed by the Angular frontend and potentially by future external integrations.

The API must provide consistent conventions for authentication, authorization, validation, errors, pagination, filtering, and resource management.

---

### 13.1 API Architecture

The initial backend API will use REST over HTTPS.

Conceptually:

```text
Angular Frontend
       │
       │ HTTPS / JSON
       ▼
Spring Boot REST API
       │
       ├── Authentication / Authorization
       ├── Business Logic
       ├── Validation
       └── Persistence
             │
             ▼
         PostgreSQL
```

The frontend must not communicate directly with PostgreSQL or other internal infrastructure.

All business operations must pass through the backend.

---

### 13.2 API Base Path

The API should use a consistent base path.

Example:

```text
/api/v1
```

Resources are then represented using predictable URLs.

Examples:

```text
GET    /api/v1/customers
GET    /api/v1/customers/{customerId}
POST   /api/v1/customers
PATCH  /api/v1/customers/{customerId}
DELETE /api/v1/customers/{customerId}
```

Versioning allows future API changes without immediately breaking existing clients.

---

### 13.3 Resource-Oriented URLs

URLs should represent resources rather than actions.

Prefer:

```text
POST /api/v1/work-orders
```

instead of:

```text
POST /api/v1/create-work-order
```

Prefer:

```text
GET /api/v1/work-orders/{id}
```

instead of:

```text
GET /api/v1/get-work-order/{id}
```

Business actions that cannot naturally be represented as CRUD operations may use explicit action endpoints where appropriate.

Examples:

```text
POST /api/v1/work-orders/{id}/assign
POST /api/v1/work-orders/{id}/start
POST /api/v1/work-orders/{id}/complete
POST /api/v1/work-orders/{id}/cancel
```

These actions must still enforce the Work Order state machine.

---

### 13.4 HTTP Methods

The API should use HTTP methods consistently.

Typical usage:

```text
GET
    Retrieve resources

POST
    Create resources or perform explicit state-changing actions

PATCH
    Partially update a resource

PUT
    Replace a resource where full replacement is appropriate

DELETE
    Remove a resource where deletion is permitted
```

The application should avoid using `POST` as a generic replacement for every operation.

---

### 13.5 Authentication

Protected endpoints require authentication.

The initial authentication mechanism will use JWT-based authentication.

Typical flow:

```text
Angular
   │
   │ POST /api/v1/auth/login
   ▼
Spring Boot
   │
   │ Authenticate
   ▼
JWT Access Token
   │
   ▼
Angular
   │
   │ Authorization: Bearer <token>
   ▼
Protected API
```

The backend must validate the token before processing protected requests.

---

### 13.6 Authorization

Authentication establishes who the user is.

Authorization determines what the user is allowed to do.

Every protected operation must evaluate authorization.

For example:

```text
User authenticated?
        ↓
Tenant membership valid?
        ↓
Required permission?
        ↓
Resource belongs to accessible tenant/customer?
        ↓
Business operation allowed?
```

The frontend must never be considered the final authorization layer.

---

### 13.7 Tenant Context

Tenant context must be derived from authenticated identity and membership.

The backend must not blindly trust a tenant ID supplied by the frontend.

For example, sending:

```text
{
    "tenantId": "tenant-123"
}
```

must not grant the user access to that tenant.

The backend determines the user's valid tenant context from authentication and authorization data.

Every tenant-scoped database query must use that context.

---

### 13.8 Request Validation

Incoming API data must be validated on the backend.

Examples:

- Required fields
- String length
- Email format
- Numeric ranges
- Dates
- Enum values
- Relationships between fields
- Business constraints

Example:

```text
Work Order
    scheduledStart < scheduledEnd
```

Validation should occur before invalid data reaches business logic or persistence.

Bean Validation will be used where appropriate.

---

### 13.9 Validation Errors

Validation failures should return a consistent response structure.

Conceptually:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": [
    {
      "field": "scheduledEnd",
      "message": "must be after scheduledStart"
    }
  ]
}
```

The frontend should be able to associate field-level errors with the corresponding form controls.

---

### 13.10 API Error Structure

Errors should use a consistent structure across the application.

Possible fields:

```text
code
message
timestamp
path
correlationId
fieldErrors
```

Example:

```json
{
  "code": "WORK_ORDER_NOT_FOUND",
  "message": "Work Order was not found",
  "timestamp": "2026-09-24T10:30:00Z",
  "path": "/api/v1/work-orders/123",
  "correlationId": "abc-123"
}
```

Error messages must not expose sensitive internal implementation details.

Database exceptions, stack traces, SQL statements, credentials, or internal infrastructure information must not be returned to clients.

---

### 13.11 HTTP Status Codes

The API should use HTTP status codes consistently.

Common responses include:

```text
200 OK
    Successful request

201 Created
    Resource successfully created

204 No Content
    Successful operation with no response body

400 Bad Request
    Invalid request

401 Unauthorized
    Authentication required or invalid

403 Forbidden
    Authenticated but not authorized

404 Not Found
    Resource does not exist or is not accessible

409 Conflict
    Request conflicts with current resource/business state

422 Unprocessable Entity
    Request structure is valid but business validation fails

429 Too Many Requests
    Rate limit exceeded

500 Internal Server Error
    Unexpected server failure
```

The exact use of `400`, `409`, and `422` will be standardized during implementation.

---

### 13.12 Business State Conflicts

Business state transitions must be validated by the backend.

For example:

```text
COMPLETED
    ↓
IN_PROGRESS
```

If the transition is not permitted, the API should reject the request rather than silently modifying the state.

A suitable response may use:

```text
409 Conflict
```

with a meaningful error code.

---

### 13.13 Pagination

Collection endpoints should support pagination.

Example:

```text
GET /api/v1/work-orders?page=0&size=20
```

The response should contain the requested records and sufficient pagination metadata.

Conceptually:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 145,
  "totalPages": 8
}
```

The exact pagination implementation will be finalized during backend development.

---

### 13.14 Filtering

Collection endpoints may support filtering.

Example:

```text
GET /api/v1/work-orders?status=IN_PROGRESS
```

Multiple filters may be combined.

Example:

```text
GET /api/v1/work-orders?status=IN_PROGRESS&priority=HIGH
```

Filtering must remain authorization-aware.

A filter must never allow a user to access records outside their permitted scope.

---

### 13.15 Sorting

Collection endpoints may support sorting.

Example:

```text
GET /api/v1/work-orders?sort=scheduledStart,asc
```

The API should define which fields may be used for sorting.

Clients should not be able to inject arbitrary database expressions through sorting parameters.

---

### 13.16 Resource Relationships

Related resources should be represented consistently.

For example:

```text
GET /api/v1/customers/{customerId}/locations
GET /api/v1/customers/{customerId}/equipment
GET /api/v1/work-orders/{workOrderId}/assignments
GET /api/v1/work-orders/{workOrderId}/files
```

Nested URLs should be used when the relationship is meaningful and the parent resource provides a natural security boundary.

The API should avoid excessively deep nesting.

---

### 13.17 DTOs

The API should use DTOs rather than exposing JPA entities directly.

For example:

```text
Entity
   ↓
Service
   ↓
Response DTO
   ↓
REST Controller
```

and:

```text
REST Controller
   ↓
Request DTO
   ↓
Service
   ↓
Entity
```

This prevents persistence models from becoming accidental public API contracts.

DTOs also allow API responses to contain only the information appropriate for the requesting user.

---

### 13.18 Create vs Update Models

Create and update requests should use appropriate DTOs.

For example:

```text
CreateWorkOrderRequest
UpdateWorkOrderRequest
WorkOrderResponse
```

The client should not be allowed to modify fields that are controlled by the backend.

For example, a client should not directly set:

```text
createdAt
createdBy
tenantId
audit information
```

These values are controlled by the server.

---

### 13.19 API Idempotency

Operations that may be retried must be designed carefully.

This is particularly important for:

- Payment webhooks
- External integrations
- Background jobs
- Retryable commands
- Potentially expensive operations

Where appropriate, idempotency keys or unique business constraints should prevent the same operation from being applied multiple times.

---

### 13.20 API Transactions

Operations that modify multiple related records should use appropriate database transactions.

For example:

```text
Complete Work Order
      │
      ├── Change Work Order status
      ├── Record completion information
      ├── Consume parts
      ├── Create audit event
      └── Trigger business event
```

The transaction boundaries must be carefully designed.

External systems such as email providers or payment providers should not be treated as if they were part of the same database transaction.

Reliable asynchronous patterns may be introduced later where required.

---

### 13.21 API Documentation

The REST API should be documented using OpenAPI.

Documentation should describe:

- Endpoints
- HTTP methods
- Request parameters
- Request bodies
- Response bodies
- Authentication
- Authorization requirements
- Validation rules
- Error responses
- Pagination
- Filtering
- Example requests/responses

Swagger UI may be used during development.

---

### 13.22 API Security

The API must use HTTPS in production.

Security controls may include:

- JWT validation
- Role/permission authorization
- Tenant isolation
- Input validation
- Rate limiting
- CORS configuration
- Secure HTTP headers
- File-upload validation
- Protection against common web vulnerabilities

Sensitive information must not be logged unnecessarily.

Authentication credentials and tokens must never be written to normal application logs.

---

### 13.23 Correlation IDs

API requests should support a correlation ID.

The correlation ID allows a request to be traced through:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Database
     ↓
Audit Event
     ↓
Kafka/Event Processing
     ↓
Notification
```

This becomes particularly important when asynchronous processing is introduced.

---

### 13.24 API Versioning

The initial API will use:

```text
/api/v1
```

Breaking API changes should result in a new version rather than silently changing the behavior of existing clients.

Backward-compatible additions may remain within the existing version where appropriate.

---

### 13.25 Business Rules

Initial rules:

1. The backend exposes a REST API over HTTPS.
2. API endpoints use a consistent versioned base path.
3. URLs should represent resources rather than arbitrary controller actions.
4. Explicit action endpoints may be used for meaningful state-changing business operations.
5. Protected endpoints require authentication.
6. Authorization must be enforced by the backend.
7. Tenant context must come from authenticated identity and membership.
8. Client-supplied tenant IDs must never bypass tenant authorization.
9. Request data must be validated on the backend.
10. API errors must use a consistent structure.
11. API responses must not expose sensitive internal implementation details.
12. HTTP status codes should be used consistently.
13. Business state transitions must be validated by the backend.
14. Collection endpoints should support pagination where appropriate.
15. Filtering and sorting must remain authorization-aware.
16. JPA entities should not be exposed directly as API contracts.
17. DTOs should be used for API requests and responses.
18. Server-controlled fields must not be client-modifiable.
19. Retryable operations must be designed with idempotency where appropriate.
20. Multi-record business operations must use appropriate transaction boundaries.
21. The API should be documented using OpenAPI.
22. Production API communication must use HTTPS.
23. Authentication credentials and tokens must not be logged.
24. Correlation IDs should support request tracing.
25. Breaking API changes should use a new API version.
26. Cross-tenant access must be explicitly prevented and tested at the API layer.

## 14. Database Design and Persistence

PostgreSQL will be the primary relational database for WorkFlow SaaS.

The database must represent the business domain accurately while enforcing data integrity, tenant isolation, and important business constraints.

Spring Data JPA and Hibernate will be used as the primary persistence technology.

Database schema changes will be managed using Flyway migrations.

---

### 14.1 Relational Database

The initial production database will use PostgreSQL.

PostgreSQL will store:

- Users
- Tenant memberships
- Roles and permissions
- Tenants
- Subscriptions
- Customers
- Locations
- Equipment
- Service Requests
- Work Orders
- Technicians
- Skills and certifications
- Assignments
- Schedules
- Maintenance Plans
- Inventory
- Invoices
- Notifications
- File metadata
- Audit events

Large binary files will not normally be stored directly in PostgreSQL.

File content will be stored in object storage, while PostgreSQL stores the associated metadata.

---

### 14.2 Entity Relationships

The database should represent the relationships established in the product specification.

A simplified structure is:

```text id="i8y4m5"
Tenant
  │
  ├── Users / Memberships
  ├── Customers
  ├── Technicians
  ├── Service Requests
  ├── Work Orders
  ├── Equipment
  ├── Inventory
  ├── Maintenance Plans
  ├── Invoices
  ├── Notifications
  └── Audit Events
```

Customer-specific data follows:

```text id="o8w4la"
Customer
   │
   ├── Customer Users
   ├── Locations
   │      └── Equipment
   ├── Service Requests
   ├── Work Orders
   └── Invoices
```

The exact physical schema will be defined during database design.

---

### 14.3 Primary Keys

Every persistent entity should have a unique primary key.

The implementation will use a consistent identifier strategy.

The identifier type and generation strategy will be selected during technical design.

The chosen strategy should provide:

- Uniqueness
- Efficient indexing
- Safe use in APIs
- No accidental exposure of sensitive information
- Compatibility with distributed processing where required

---

### 14.4 Foreign Keys

Relationships between entities should be represented using foreign keys wherever appropriate.

Examples:

```text id="qrxm0p"
work_order.customer_id
work_order.service_request_id
work_order.tenant_id
```

Foreign keys protect referential integrity.

The application should not rely exclusively on Java code to prevent references to nonexistent records.

---

### 14.5 Tenant ID

Tenant-scoped entities should contain a tenant reference where appropriate.

For example:

```text id="j7q6a4"
customers
    tenant_id

service_requests
    tenant_id

work_orders
    tenant_id

invoices
    tenant_id

inventory_items
    tenant_id
```

The exact placement of tenant identifiers will be determined during schema design.

The purpose is to make tenant boundaries explicit and make tenant-scoped querying efficient and safe.

---

### 14.6 Tenant Isolation at Database Level

Tenant isolation must be enforced by the application and supported by the database design.

Every tenant-scoped query must include the appropriate tenant context.

Conceptually:

```sql id="z2y9c1"
SELECT *
FROM work_orders
WHERE tenant_id = :tenantId;
```

The backend must never retrieve all tenant records and rely on the frontend to filter them.

Database access patterns should make accidental cross-tenant queries difficult.

Additional PostgreSQL mechanisms such as Row-Level Security may be evaluated later if appropriate.

---

### 14.7 Unique Constraints

Important business uniqueness rules should be enforced at the database level.

Examples may include:

- User email
- Tenant identifier
- Customer reference within a tenant
- Equipment serial number where applicable
- SKU within a tenant
- Invoice number within a tenant

The exact uniqueness rules depend on the business domain.

Tenant-scoped uniqueness should normally include the tenant boundary.

For example:

```text id="5t3m4f"
Tenant A + Customer Code 100
Tenant B + Customer Code 100
```

may both be valid.

The database constraint should therefore reflect the actual business rule.

---

### 14.8 Indexes

Indexes should be created for frequently queried fields.

Likely candidates include:

- `tenant_id`
- User email
- Customer name
- Work Order status
- Work Order priority
- Work Order scheduled time
- Service Request status
- Service Request priority
- Equipment serial number
- Invoice number
- Invoice status
- Notification recipient
- Notification read status
- Audit event timestamp
- Foreign key columns

Indexes should be added based on actual query patterns rather than indiscriminately indexing every column.

Too many indexes increase storage requirements and can negatively affect write performance.

---

### 14.9 Composite Indexes

Some queries will commonly filter by multiple fields.

Examples:

```text id="7y6w1h"
tenant_id + status
tenant_id + scheduled_start
tenant_id + customer_id
tenant_id + priority
tenant_id + created_at
```

Composite indexes may be appropriate for these access patterns.

The exact indexes will be determined from expected queries and later verified using database performance analysis.

---

### 14.10 Timestamps

Persistent entities should use appropriate timestamps where historical information matters.

Common fields include:

```text id="h9j8vl"
created_at
updated_at
```

Additional business timestamps may include:

```text id="9f9j9j"
assigned_at
scheduled_start
actual_start
completed_at
cancelled_at
issued_at
paid_at
```

Creation/update timestamps describe persistence history.

Business timestamps describe domain events.

These concepts should not be confused.

---

### 14.11 Soft Deletion

The system should not automatically use physical deletion for every entity.

Some business records have historical or audit importance.

For appropriate entities, the system may use a lifecycle such as:

```text id="x5b8t6"
ACTIVE
   ↓
ARCHIVED / INACTIVE
```

rather than physically deleting the record.

Examples may include:

- Customers
- Equipment
- Users
- Inventory Items
- Maintenance Plans

Financial and audit records should generally preserve historical information.

The exact deletion/archival strategy will be defined per entity.

---

### 14.12 Referential Integrity and Deletion

Deleting a record that other business records depend on must be handled carefully.

For example, deleting a Customer should not automatically destroy:

- Historical Work Orders
- Service Requests
- Invoices
- Equipment history
- Audit records

Where historical relationships must remain intact, the application should use deactivation or archival rather than destructive deletion.

Cascade deletion should therefore be used deliberately rather than as a default.

---

### 14.13 Enumerated States

Business states such as:

```text id="x3f3h1"
WorkOrderStatus
ServiceRequestStatus
InvoiceStatus
SubscriptionStatus
UserStatus
```

must be represented consistently.

The implementation approach may use Java enums with corresponding database representations.

The database must not allow arbitrary invalid state values.

---

### 14.14 Transactions

Operations that modify multiple related records must use database transactions where appropriate.

For example:

```text id="1w6qjg"
Work Order Completion
        │
        ├── Update Work Order
        ├── Record labor
        ├── Record consumed parts
        ├── Update inventory
        └── Create audit information
```

The transaction boundary must ensure that related database changes do not leave the system in an inconsistent state.

External systems such as email providers, payment providers, or object storage must not automatically be assumed to participate in the same PostgreSQL transaction.

---

### 14.15 Optimistic Locking

Entities that may be modified concurrently should support optimistic locking where appropriate.

For example:

```text id="o8yr2c"
Manager A opens Work Order
Manager B opens same Work Order
Manager A updates it
Manager B attempts update
```

The system should be able to detect that the entity changed between reads and writes.

JPA optimistic locking may be implemented using a version field.

This helps prevent accidental overwriting of newer data.

---

### 14.16 Database Migrations

Database schema changes will be managed using Flyway.

Schema changes should be versioned.

Conceptually:

```text id="y4b4ar"
V1__initial_schema.sql
V2__add_work_order_assignments.sql
V3__add_maintenance_plans.sql
...
```

Migrations should be:

- Version-controlled
- Reproducible
- Applied consistently across environments
- Reviewed as part of development
- Safe to execute in deployment pipelines

Developers should not manually modify production schemas outside the migration process.

---

### 14.17 JPA and Hibernate

Spring Data JPA and Hibernate will provide the primary object-relational mapping layer.

The application should use entities to represent persistence models while keeping domain/business logic appropriately separated from persistence concerns.

JPA relationships should be designed carefully.

The application should avoid unnecessarily loading large object graphs.

Particular attention should be paid to:

- Lazy vs eager loading
- N+1 queries
- Fetch joins
- Entity graphs
- Pagination
- Transaction boundaries
- Cascade behavior

---

### 14.18 N+1 Query Prevention

The application must monitor for N+1 query problems.

For example:

```text id="9gk9y7"
Load 100 Work Orders
        ↓
100 additional queries for Customers
        ↓
100 additional queries for Technicians
```

This can create severe performance problems.

The implementation should use appropriate query strategies such as:

- Fetch joins
- Entity graphs
- Explicit projections
- Batch fetching

The correct solution should be selected based on the specific query rather than globally forcing eager loading.

---

### 14.19 DTO and Persistence Separation

Database entities should not automatically become REST API responses.

The architecture should maintain a separation between:

```text id="4o1c4v"
Database Entity
      ↓
Repository
      ↓
Service / Domain Logic
      ↓
DTO
      ↓
REST API
```

This prevents persistence implementation details from becoming public API contracts.

---

### 14.20 Database Auditing

Important persistence changes may use automatic timestamps or auditing mechanisms.

However, technical persistence auditing is not a replacement for business Audit Events.

For example:

```text id="s1r4gc"
updated_at
```

answers:

> When was this database record last changed?

An Audit Event answers:

> Who changed the Work Order status from IN_PROGRESS to COMPLETED and when?

Both concepts may be required.

---

### 14.21 Database Performance

Database performance should be measured rather than optimized blindly.

Important areas include:

- Query execution time
- Index usage
- Slow queries
- Connection pool usage
- Lock contention
- Transaction duration
- Database CPU and memory
- Table growth
- Index growth

Production monitoring will later integrate with the observability stack.

---

### 14.22 Backup and Recovery

The production PostgreSQL database must have a backup and recovery strategy.

The final infrastructure design should address:

- Automated backups
- Backup retention
- Point-in-time recovery where available
- Recovery testing
- Disaster recovery
- Database availability

A backup is only useful if the organization can successfully restore it.

Recovery procedures must therefore be tested.

---

### 14.23 Business Rules

Initial rules:

1. PostgreSQL is the primary relational database.
2. Flyway manages schema migrations.
3. Persistent entities use consistent primary keys.
4. Business relationships should use foreign keys where appropriate.
5. Tenant-scoped entities must preserve tenant boundaries.
6. Tenant isolation must be enforced by backend authorization and supported by database design.
7. Important business uniqueness rules must be enforced at the database level.
8. Indexes should be based on actual query patterns.
9. Composite indexes may be used for common tenant-scoped queries.
10. Persistence timestamps and business timestamps represent different concepts.
11. Historical business records must not be deleted casually.
12. Cascade deletion must be used deliberately.
13. Invalid business states must not be accepted by the database.
14. Multi-record operations must use appropriate transaction boundaries.
15. Optimistic locking should be used where concurrent modifications are possible.
16. Database schema changes must be version-controlled through Flyway.
17. JPA relationships must be designed to avoid unnecessary data loading.
18. N+1 query problems must be actively prevented and monitored.
19. Persistence entities must remain separate from public API DTOs.
20. Technical database auditing does not replace business audit events.
21. Database performance must be measured using real query behavior.
22. Production databases require tested backup and recovery procedures.
23. Cross-tenant database access must be explicitly prevented and tested.

## 15. Backend Architecture and Module Structure

The initial WorkFlow SaaS backend will be implemented as a modular monolith using Java and Spring Boot.

The application will initially be deployed as a single backend application while maintaining clear internal module boundaries.

The architecture should avoid premature microservices while still allowing individual domains to evolve independently.

---

### 15.1 Modular Monolith

The initial architecture is:

```text id="5h3xqk"
                 WorkFlow SaaS Backend
                         │
        ┌────────────────┼────────────────┐
        │                │                │
   Identity          Operations        Customers
        │                │                │
   Billing          Inventory        Maintenance
        │                │                │
   Notifications      Files            Audit
                         │
                         ▼
                    PostgreSQL
```

These modules initially run inside the same Spring Boot application.

They should communicate through well-defined interfaces rather than directly accessing each other's internal implementation details.

---

### 15.2 Initial Modules

The backend will be organized around business capabilities.

Initial modules may include:

```text id="s1zj8m"
identity
tenancy
subscription
customer
equipment
service-request
work-order
technician
scheduling
maintenance
inventory
billing
notification
file
audit
reporting
```

The exact package/module names may evolve during implementation.

The important principle is that the structure follows business domains rather than placing every class into large technical packages.

---

### 15.3 Avoiding the Giant Package Structure

The project should avoid a structure such as:

```text
controller/
service/
repository/
entity/
dto/
```

containing hundreds of unrelated classes.

Although technically valid, this structure makes business boundaries difficult to understand as the application grows.

Instead, the preferred structure is domain-oriented.

Conceptually:

```text id="k0gq6f"
workorder/
    controller/
    service/
    repository/
    domain/
    dto/

customer/
    controller/
    service/
    repository/
    domain/
    dto/

inventory/
    controller/
    service/
    repository/
    domain/
    dto/
```

This keeps related functionality together.

---

### 15.4 Module Boundaries

Each module should have a clearly defined responsibility.

For example:

**Customer module**

Responsible for:

- Customers
- Customer lifecycle
- Customer users
- Customer-specific business rules

**Work Order module**

Responsible for:

- Work Order lifecycle
- Work Order state transitions
- Completion
- Failure
- Cancellation
- Work Order business rules

**Inventory module**

Responsible for:

- Inventory items
- Stock
- Stock movements
- Stock validation
- Parts consumption

The Work Order module should not directly manipulate Inventory database tables.

Instead, it should communicate with the Inventory module through an appropriate service/interface.

---

### 15.5 Dependency Direction

Modules should have controlled dependencies.

For example:

```text id="g5v7e8"
Work Order
    │
    ├── Customer
    ├── Technician
    ├── Scheduling
    └── Inventory
```

However, these relationships must not become uncontrolled circular dependencies.

For example:

```text id="p2l3u6"
Work Order → Inventory
Inventory → Work Order
```

should not automatically mean that both modules directly depend on each other's internal services and repositories.

Where bidirectional business communication is required, domain events or carefully defined interfaces may be used.

---

### 15.6 Controllers

Controllers are responsible for the HTTP/API boundary.

A controller should:

- Receive HTTP requests
- Validate request structure
- Authenticate/authorize through the security layer
- Convert requests into application commands
- Return appropriate responses

Controllers should not contain complex business logic.

For example, a controller should not implement:

```text id="5z6y9c"
if workOrder.status == ...
    update inventory
    assign technician
    create invoice
    send email
```

That logic belongs in the appropriate application/domain services.

---

### 15.7 Application Services

Application services coordinate business operations.

Examples:

```text id="6e0a5q"
WorkOrderService
ServiceRequestService
CustomerService
InventoryService
MaintenanceService
InvoiceService
```

An application service may coordinate multiple domain operations within a transaction.

For example:

```text id="l6x0m4"
completeWorkOrder()
       │
       ├── Validate transition
       ├── Record completion
       ├── Consume parts
       ├── Record audit event
       └── Publish business event
```

The exact distribution of responsibilities between application services and domain objects will be determined during implementation.

---

### 15.8 Domain Logic

Important business rules should not exist exclusively inside controllers.

Examples of domain rules include:

- Work Order state transitions
- Service Request state transitions
- Inventory stock validation
- Invoice state transitions
- Maintenance scheduling rules
- Technician assignment constraints

Business logic should be placed where it can be reused by:

- REST APIs
- Background jobs
- Event consumers
- Future integrations
- Automated tests

---

### 15.9 Repositories

Repositories provide persistence access.

They should encapsulate database interaction rather than being called directly from controllers.

Conceptually:

```text id="w4m8fz"
Controller
    ↓
Application Service
    ↓
Domain / Business Logic
    ↓
Repository
    ↓
PostgreSQL
```

Repositories should expose the queries required by the business/application layer rather than becoming generic uncontrolled database access points.

---

### 15.10 DTOs

Modules should define API DTOs appropriate to their public contracts.

Examples:

```text id="9cx4pu"
CreateCustomerRequest
UpdateCustomerRequest
CustomerResponse

CreateWorkOrderRequest
UpdateWorkOrderRequest
WorkOrderResponse
```

DTOs should not expose persistence-specific implementation details.

Different operations may require different DTOs.

---

### 15.11 Security Module

Security is cross-cutting but should have a clear architectural boundary.

The security layer is responsible for:

- Authentication
- JWT validation
- User identity
- Tenant context
- Role/permission checks
- Access control
- Security configuration

Business modules should not implement their own authentication mechanisms.

They may, however, request authorization decisions through defined security mechanisms.

---

### 15.12 Tenant Context

Tenant context is a critical cross-cutting concern.

A request should establish the authenticated user's tenant context before tenant-scoped business operations execute.

Conceptually:

```text id="h7q1b3"
HTTP Request
     ↓
Authentication
     ↓
User Identity
     ↓
Tenant Membership
     ↓
Tenant Context
     ↓
Business Operation
```

Tenant context must be propagated correctly into:

- Database operations
- Background jobs
- Notifications
- Events
- Cache operations
- File access
- Reports

A missing tenant context should fail safely rather than defaulting to an arbitrary tenant.

---

### 15.13 Cross-Cutting Infrastructure

Some capabilities are shared across modules.

Examples include:

- Security
- Tenant context
- Error handling
- Validation
- Logging
- Correlation IDs
- Auditing
- Event publishing
- Transaction management
- File storage
- Notification delivery

These should be centralized where appropriate without turning the infrastructure layer into a dumping ground for business logic.

---

### 15.14 Domain Events

Modules may publish domain/business events when important things happen.

Examples:

```text id="x9r4ab"
WorkOrderCompleted
WorkOrderAssigned
ServiceRequestApproved
MaintenanceDue
InvoiceIssued
SubscriptionPaymentFailed
```

An event describes something that has already happened.

For example:

```text id="c5x3s8"
Work Order completed
        ↓
WorkOrderCompleted
        ↓
    Consumers
      ├── Audit
      ├── Notification
      └── Reporting
```

Events should not automatically be introduced for every method call.

They should be used when asynchronous processing, decoupling, or integration provides real value.

---

### 15.15 Kafka

Kafka will be introduced later where event-driven processing provides meaningful benefits.

Potential use cases include:

- Notifications
- Audit processing
- Reporting events
- Integration events
- Large asynchronous workflows
- External integrations

The initial MVP should not depend on Kafka for basic CRUD operations.

For example, creating a Customer does not require Kafka merely because Kafka exists in the technology stack.

---

### 15.16 Transaction Boundaries

Business operations should define explicit transaction boundaries.

For example:

```text id="6o8r1e"
Complete Work Order
       │
       ├── Validate
       ├── Update Work Order
       ├── Record labor
       ├── Consume inventory
       └── Persist required business state
```

These database changes should be committed consistently.

External side effects such as sending an email should generally occur outside the database transaction or use a reliable event/outbox mechanism.

---

### 15.17 Outbox Pattern

When an important database transaction must reliably result in an asynchronous event, the application may use the transactional outbox pattern.

Conceptually:

```text id="9l6k2v"
Database Transaction
       │
       ├── Business Data
       │
       └── Outbox Event
                │
                ▼
          Event Publisher
                │
                ▼
              Kafka
```

This prevents a situation where the business transaction succeeds but event publication fails silently.

The outbox pattern will be introduced only where the reliability requirement justifies its complexity.

---

### 15.18 Background Jobs

Some operations should execute asynchronously or periodically.

Examples:

- Maintenance due checks
- Overdue Work Order detection
- Notification retries
- Subscription checks
- Report generation
- Cleanup tasks
- Usage calculations

Background jobs must establish the correct tenant context when processing tenant-specific data.

They must never accidentally process records across tenants without explicit authorization and business purpose.

---

### 15.19 External Integrations

External services should be isolated behind dedicated integration components.

Potential integrations include:

- Stripe
- Email provider
- AWS S3
- Kafka
- Future third-party APIs

Business modules should not contain raw HTTP calls to external providers throughout the codebase.

Instead:

```text id="6r6jtu"
Billing Module
      ↓
Payment Provider Interface
      ↓
Stripe Adapter
```

This makes external dependencies easier to test and replace.

---

### 15.20 Configuration

Environment-specific configuration must not be hardcoded into business logic.

Configuration may include:

- Database connection
- JWT configuration
- Email provider credentials
- Object storage credentials
- Kafka configuration
- Redis configuration
- Payment provider configuration

Secrets must be provided through secure environment/configuration mechanisms.

Secrets must never be committed to Git.

---

### 15.21 Testing Boundaries

The modular architecture should support different levels of testing.

Examples:

```text id="u5z8yw"
Unit Tests
    ↓
Business logic

Integration Tests
    ↓
Database / repositories / Spring components

API Tests
    ↓
REST endpoints + security

End-to-End Tests
    ↓
Complete user workflows
```

Modules should be testable without requiring the entire infrastructure stack for every unit test.

---

### 15.22 Architecture Evolution

The modular monolith is the initial architecture, not a permanent restriction.

If a module eventually requires independent:

- Scaling
- Deployment
- Availability
- Ownership
- Technology
- Processing capacity

it may become a candidate for extraction into a separate service.

However, extraction should be based on demonstrated requirements rather than theoretical future scale.

---

### 15.23 Business Rules

Initial rules:

1. WorkFlow SaaS will initially use a modular monolith architecture.
2. Business modules should be organized around domain capabilities.
3. Controllers should remain focused on the API boundary.
4. Business logic should not be implemented inside controllers.
5. Application services coordinate business operations.
6. Repositories provide controlled persistence access.
7. DTOs separate API contracts from persistence entities.
8. Security has a defined cross-cutting boundary.
9. Tenant context must be established before tenant-scoped operations.
10. Tenant context must propagate correctly through asynchronous processing.
11. Modules should avoid uncontrolled circular dependencies.
12. Domain/business events should be used where they provide meaningful decoupling or asynchronous behavior.
13. Kafka should not be introduced for simple synchronous CRUD operations without a real requirement.
14. Multi-record business operations require appropriate transaction boundaries.
15. The transactional outbox pattern may be used for reliable event publication.
16. Background jobs must preserve correct tenant context.
17. External integrations should be isolated behind dedicated integration components.
18. Secrets must never be committed to source control.
19. The architecture must support unit, integration, API, and end-to-end testing.
20. Microservice extraction should be driven by demonstrated technical or business requirements.

## 16. Frontend Architecture and Angular Application Structure

The WorkFlow SaaS frontend will be implemented using Angular and TypeScript.

The frontend will provide the user interface for the WorkFlow SaaS REST API and will be responsible for presentation, navigation, user interaction, client-side validation, and application state required for the user experience.

The backend remains the authoritative source for authentication, authorization, business rules, tenant isolation, and persistent business state.

---

### 16.1 Frontend Architecture

The initial frontend architecture will be organized around business domains.

Conceptually:

```text id="l4g7p2"
Angular Application
       │
       ├── Authentication
       ├── Dashboard
       ├── Customers
       ├── Equipment
       ├── Service Requests
       ├── Work Orders
       ├── Technicians
       ├── Scheduling
       ├── Maintenance
       ├── Inventory
       ├── Invoices
       ├── Notifications
       ├── Reports
       └── Administration
```

The exact Angular folder structure may evolve during implementation.

The important principle is that frontend organization should remain aligned with the application's business domains.

---

### 16.2 Angular Feature Modules / Areas

The frontend should use feature-oriented organization rather than placing all components into large global folders.

Conceptually:

```text id="a4n1ps"
features/
    auth/
    dashboard/
    customers/
    equipment/
    service-requests/
    work-orders/
    technicians/
    scheduling/
    maintenance/
    inventory/
    invoices/
    notifications/
    reports/
    administration/
```

Shared functionality should be separated from domain-specific functionality.

---

### 16.3 Shared Frontend Components

Reusable UI components may include:

- Buttons
- Dialogs
- Tables
- Pagination
- Form controls
- Date/time controls
- Status badges
- Loading indicators
- Error displays
- Confirmation dialogs
- File upload components
- Notification components

Shared components should remain generic enough to be reused without containing business-specific rules.

---

### 16.4 Angular Services

Frontend services should encapsulate communication with the backend and shared client-side behavior.

Examples:

```text id="q7m0dn"
AuthService
CustomerService
WorkOrderService
ServiceRequestService
TechnicianService
MaintenanceService
InventoryService
InvoiceService
NotificationService
```

A service should provide a clear interface to the rest of the frontend.

Components should not contain repeated raw HTTP calls.

---

### 16.5 HTTP Communication

Angular will communicate with Spring Boot using HTTP/HTTPS and JSON.

Conceptually:

```text id="w5h7q0"
Angular Component
       ↓
Angular Service
       ↓
HttpClient
       ↓
REST API
       ↓
Spring Boot
```

HTTP communication should be centralized through appropriate services and interceptors.

---

### 16.6 Authentication State

The frontend must maintain the authenticated user's session state.

It may need to know:

- Whether the user is authenticated
- User identity
- Tenant context available to the user
- Roles
- Permissions
- Token/session state
- Session expiration

The frontend may use this information to control navigation and presentation.

However, this information must never be treated as proof of authorization.

The backend remains authoritative.

---

### 16.7 Authentication Interceptor

An Angular HTTP interceptor may automatically attach the authentication token to protected API requests.

Conceptually:

```text id="c9r2kx"
HTTP Request
     ↓
Auth Interceptor
     ↓
Authorization Header
     ↓
Spring Boot API
```

The interceptor may also handle common authentication-related responses such as expired sessions.

Sensitive tokens must be handled according to the final authentication architecture.

---

### 16.8 Route Guards

Angular route guards may prevent unauthenticated users from navigating to protected pages.

Examples:

```text id="3n7v5k"
/login
    ↓
/dashboard
    ↓
/work-orders
    ↓
/administration
```

A route guard may check whether the user has the required role or permission for a route.

However:

> Route guards are a user-interface convenience, not a security boundary.

A malicious client can bypass Angular completely and call the API directly.

The backend must therefore enforce the same authorization independently.

---

### 16.9 Role-Based User Interface

The frontend may adapt the interface based on the user's permissions.

For example:

```text id="9h5w2m"
Manager
    ├── Assign Work Order
    ├── Schedule Work
    └── View Workload

Technician
    ├── View Assigned Work
    ├── Start Work
    └── Complete Work

Customer
    ├── Create Request
    ├── View Requests
    └── View Invoices
```

Buttons and navigation items that the user cannot use may be hidden or disabled.

The backend must still verify every operation.

---

### 16.10 Frontend Business Rules

The frontend may implement presentation-oriented rules.

Examples:

- Display a warning when a date is invalid.
- Disable a submit button while a request is processing.
- Show a confirmation dialog before cancellation.
- Display status-specific UI.
- Prevent obviously invalid form input.

However, authoritative business rules belong to the backend.

For example, the frontend may prevent a user from selecting an obviously invalid Work Order transition, but the backend must still reject the transition if it is not allowed.

---

### 16.11 Forms and Validation

Angular reactive forms will be used for complex business forms.

Examples include:

- Customer creation
- Equipment creation
- Service Request creation
- Work Order creation
- Technician management
- Inventory operations
- Invoice creation

Validation should occur at two levels:

```text id="h0g5n1"
Angular Validation
       ↓
User experience

Backend Validation
       ↓
Security + business correctness
```

The frontend should provide immediate feedback where possible.

The backend must always repeat authoritative validation.

---

### 16.12 API Error Handling

The frontend should provide centralized handling for common API errors.

Examples:

```text id="t3b9x8"
401
    → Session/authentication handling

403
    → Access denied UI

404
    → Resource not found

409
    → Business state conflict

422
    → Validation feedback

500
    → Generic server error
```

The UI should avoid exposing raw backend exceptions or technical stack traces.

---

### 16.13 Loading States

Long-running requests should provide appropriate visual feedback.

Examples:

- Loading indicators
- Disabled submit buttons
- Skeleton loading
- Progress indicators
- Empty states

The UI should clearly distinguish:

```text id="c0v9y2"
Loading
Empty
Error
Loaded
```

These are different states and should not be represented by the same UI.

---

### 16.14 Optimistic UI Updates

Optimistic updates may be used for selected low-risk interactions.

For example:

```text id="w8y5o4"
Mark notification as read
       ↓
Update UI immediately
       ↓
Send API request
```

If the API operation fails, the frontend must restore the correct state.

Critical business operations should generally wait for backend confirmation.

Examples:

- Completing Work Order
- Issuing Invoice
- Consuming inventory
- Cancelling Work Order
- Changing subscription

---

### 16.15 Client-Side State

The frontend may maintain state for:

- Authenticated user
- Current UI state
- Filters
- Pagination
- Selected records
- Notifications
- Cached reference data
- In-progress forms

The initial application should avoid introducing a complex global state-management framework unless actual application complexity justifies it.

Angular's built-in reactive patterns and services may be sufficient initially.

A dedicated state-management solution can be introduced later if necessary.

---

### 16.16 Server State vs UI State

The application should distinguish between server state and local UI state.

**Server state:**

- Customers
- Work Orders
- Service Requests
- Invoices
- Equipment
- Inventory

The backend is authoritative.

**UI state:**

- Selected tab
- Open dialog
- Current filter
- Sort order
- Temporary form state

The frontend controls these values.

This distinction prevents the frontend from becoming a second source of truth for persistent business data.

---

### 16.17 Data Refresh

Data should be refreshed when appropriate.

Examples:

- After creating a Work Order
- After assigning a technician
- After completing a Work Order
- After changing inventory
- After issuing an invoice

Real-time updates may eventually be introduced using WebSockets.

Until then, normal request/response refresh mechanisms are sufficient.

---

### 16.18 Real-Time Communication

A future version may support WebSockets for real-time updates.

Potential use cases:

- New notifications
- Work Order assignment
- Schedule changes
- Dispatcher updates
- Technician status
- Dashboard updates

Conceptually:

```text id="k6j1r0"
Spring Boot
    ↓
WebSocket
    ↓
Angular
    ↓
UI Update
```

WebSocket messages must still respect tenant and user authorization.

A WebSocket connection must never become a way to bypass normal access control.

---

### 16.19 File Uploads

The frontend may provide file-upload functionality for:

- Service Request photos
- Work Order photos
- Equipment documents
- Certificates
- Reports
- Invoices

The frontend may validate file size and type for user experience.

The backend must perform authoritative validation and authorization.

Files should preferably be uploaded using controlled object-storage mechanisms rather than passing large binary content unnecessarily through unrelated business APIs.

---

### 16.20 Responsive Design

The application should support common desktop and mobile viewport sizes.

The technician experience is particularly important on mobile devices because technicians may use the system while working in the field.

Important mobile workflows include:

- View assigned Work Order
- Navigate to job information
- Start work
- Pause work
- Add notes
- Upload photos
- Record parts
- Complete work
- Capture customer confirmation

The initial UI should prioritize usability rather than attempting to reproduce every desktop feature on mobile.

---

### 16.21 Accessibility

The frontend should follow accessible UI practices.

Considerations include:

- Keyboard navigation
- Appropriate labels
- Focus management
- Semantic HTML
- Sufficient contrast
- Accessible error messages
- Screen-reader support
- Accessible dialogs
- Meaningful button labels

Accessibility should be considered during component development rather than treated only as a final testing phase.

---

### 16.22 Frontend Security

The frontend must not contain secrets.

Examples of information that must not be embedded in the Angular application:

- Database credentials
- JWT signing secrets
- Payment provider secret keys
- AWS secret keys
- Internal service credentials

Public configuration such as API URLs may be included where necessary.

---

### 16.23 Environment Configuration

The frontend should support environment-specific configuration.

Examples:

```text id="v4z8d1"
Development
    ↓
Local Spring Boot API

Test
    ↓
Test backend

Production
    ↓
Production API
```

Environment configuration must not contain sensitive credentials.

---

### 16.24 Frontend Error Reporting

The frontend should eventually integrate with application monitoring.

Potentially tracked information includes:

- JavaScript errors
- Failed API requests
- Navigation failures
- Important UI failures
- Performance information

Sensitive user information must not be unintentionally captured in error reports.

---

### 16.25 Frontend Testing

The frontend should support multiple testing levels.

Examples:

```text id="z5t1f7"
Unit Tests
    ↓
Components / services

Integration Tests
    ↓
Component interactions

End-to-End Tests
    ↓
Complete user workflows
```

Important workflows should eventually have end-to-end coverage.

Examples:

```text id="p4k6c3"
Customer creates Service Request
        ↓
Manager approves it
        ↓
Work Order created
        ↓
Technician assigned
        ↓
Technician completes work
        ↓
Customer confirms completion
```

---

### 16.26 Frontend and API Contracts

Frontend models should correspond to API DTOs.

Changes to the API contract should be deliberate and documented.

The frontend should not assume database entity structure.

For example, the Angular application should consume:

```text id="2x4q8w"
WorkOrderResponse
```

rather than depending on the internal JPA `WorkOrder` entity structure.

---

### 16.27 Business Rules

Initial rules:

1. Angular is responsible for presentation, navigation, and user interaction.
2. The Spring Boot backend remains authoritative for business state and authorization.
3. Frontend feature organization should follow business domains.
4. Angular services should encapsulate API communication.
5. HTTP communication should use centralized services/interceptors.
6. Route guards may protect frontend navigation but are not a security boundary.
7. The backend must independently enforce every authorization rule.
8. Frontend validation improves user experience but does not replace backend validation.
9. API errors should be handled consistently.
10. Loading, empty, error, and loaded states should be represented separately.
11. Server state and UI state should be treated as different concepts.
12. Complex global state management should only be introduced when justified by application complexity.
13. Critical business operations should wait for backend confirmation.
14. WebSockets may later provide real-time updates but must respect tenant and user authorization.
15. File uploads require backend authorization and validation.
16. The frontend must not contain secrets.
17. Environment configuration must not expose sensitive credentials.
18. The application should support responsive layouts, particularly for technician workflows.
19. Accessibility should be considered throughout frontend development.
20. Important business workflows should eventually have end-to-end test coverage.
21. Frontend models should depend on API contracts rather than database entities.
22. Cross-tenant data must never be displayed by the frontend, and the backend must independently prevent such access.

## 17. Security Architecture and Threat Model

Security is a fundamental architectural requirement of WorkFlow SaaS.

Because the platform is multi-tenant and handles operational, customer, financial, authentication, and potentially sensitive file data, security must be enforced across every application layer.

Security must not depend on frontend behavior or on users behaving correctly.

The backend and database must enforce the security boundaries.

---

### 17.1 Security Principles

The platform follows these principles:

- Authenticate every protected request.
- Authorize every protected operation.
- Enforce tenant isolation.
- Apply least-privilege access.
- Never trust client-controlled authorization data.
- Validate all external input.
- Protect credentials and secrets.
- Minimize sensitive data exposure.
- Audit important security and business operations.
- Fail securely.
- Make security controls testable.

---

### 17.2 Authentication

Authentication establishes the identity of the user.

The initial authentication system will use:

- Email/username
- Password
- JWT-based authentication

The authentication flow is conceptually:

```text id="h7v3p1"
User
  ↓
Login
  ↓
Spring Security
  ↓
Credential Verification
  ↓
JWT Access Token
  ↓
Authenticated API Requests
```

Passwords must never be stored in plaintext.

---

### 17.3 Password Security

Passwords must be stored using a strong password-hashing algorithm.

The system should use a password hashing mechanism designed specifically for passwords, such as:

- Argon2
- BCrypt

The chosen implementation must use an appropriate work factor.

Passwords must never be:

- Logged
- Stored in plaintext
- Included in API responses
- Included in JWT claims
- Stored in frontend configuration
- Sent to unrelated services

---

### 17.4 Account Security

User accounts may have states such as:

```text id="2w8f4c"
INVITED
   ↓
ACTIVE
   ↓
LOCKED / DISABLED
```

Security-related account behavior may include:

- Failed-login detection
- Temporary account lockout
- Account disabling
- Password reset
- Password change
- Session/token invalidation where appropriate

Password-reset mechanisms must use short-lived, securely generated tokens.

Password-reset tokens must not contain passwords or sensitive account information.

---

### 17.5 JWT Security

JWT access tokens should contain only the information necessary for authentication and authorization.

Possible claims include:

- User ID
- Tenant context or membership information
- Roles/permissions where appropriate
- Issued-at timestamp
- Expiration timestamp
- Token identifier where required

Sensitive business information must not be placed into JWT claims unnecessarily.

Access tokens must have a limited lifetime.

The system should eventually support secure refresh-token handling where persistent sessions are required.

---

### 17.6 Token Storage

Token storage must be selected carefully based on the final authentication architecture.

The implementation should avoid exposing long-lived authentication credentials to JavaScript when a safer architecture is available.

If browser cookies are used, appropriate controls should include:

- Secure
- HttpOnly
- Appropriate SameSite policy

If another token-storage mechanism is selected, the security implications must be evaluated explicitly.

---

### 17.7 Authorization

Authorization determines what an authenticated user may do.

Authorization is based on:

```text id="k9n1y8"
User
  ↓
Tenant Membership
  ↓
Role
  ↓
Permission
  ↓
Resource Scope
  ↓
Business Rule
```

For example, possessing:

```text
WORK_ORDER_COMPLETE
```

does not automatically allow a user to complete every Work Order in the system.

The user must also have access to the relevant tenant and resource.

---

### 17.8 Tenant Isolation

Tenant isolation is one of the most important security requirements.

A request from Tenant A must never access Tenant B data.

This applies to:

- REST APIs
- Database queries
- Search
- Reports
- Dashboard metrics
- Cache
- Files
- Notifications
- WebSockets
- Background jobs
- Kafka events
- Audit events
- Exports

Tenant ID must never be treated as a security boundary merely because it is present in a request.

The backend must derive and validate tenant context from authenticated identity and membership.

---

### 17.9 Customer Isolation

Customer-facing users have an additional security boundary.

For example:

```text id="j5x3m8"
Tenant
  │
  ├── Customer A
  │      └── Customer Users
  │
  └── Customer B
         └── Customer Users
```

A Customer A user must not access Customer B resources.

This restriction applies even though both customers belong to the same tenant.

---

### 17.10 Platform Administration

Platform Administrators operate at the SaaS platform level.

Their permissions must be explicitly separated from normal tenant administration.

A Platform Administrator may access platform-level information such as:

- Tenants
- Subscription plans
- Platform subscriptions
- Platform configuration
- Platform health
- Platform audit information

Tenant administrators should not automatically receive platform-level permissions.

Platform administrative operations must be strongly protected and audited.

---

### 17.11 Object-Level Authorization

Authorization must occur at the resource level.

For example:

```text id="e3j6v1"
GET /api/v1/work-orders/123
```

The backend must verify:

1. The user is authenticated.
2. The user belongs to the appropriate tenant.
3. The user has permission to view Work Orders.
4. Work Order `123` belongs to an accessible scope.
5. Any additional customer/resource restrictions are satisfied.

Checking only the user's role is insufficient.

---

### 17.12 IDOR Protection

The system must protect against insecure direct object references.

For example, a malicious user should not be able to change:

```text id="0n9q3x"
GET /api/v1/work-orders/123
```

to:

```text id="4m8s7y"
GET /api/v1/work-orders/124
```

and receive another user's or tenant's Work Order.

Every resource lookup must enforce authorization.

This must be explicitly tested.

---

### 17.13 Input Validation

All client-controlled input must be treated as untrusted.

Validation should cover:

- Request bodies
- Query parameters
- Path variables
- Headers where applicable
- File metadata
- Uploaded content
- External webhook payloads

Validation must occur on the backend even when Angular already validates the same data.

---

### 17.14 SQL Injection

The application must prevent SQL injection.

JPA/Hibernate parameterized queries should be used rather than constructing SQL from raw user input.

Dynamic queries must also safely bind parameters.

User-controlled values must never be concatenated directly into SQL statements.

---

### 17.15 Cross-Site Scripting

The application must protect against XSS.

Angular provides built-in protections for many common cases, but security must still be considered when handling:

- HTML content
- Rich text
- User-generated content
- Uploaded files
- External URLs
- Dynamic templates

The application should avoid rendering untrusted HTML unless it is explicitly sanitized and required.

---

### 17.16 Cross-Site Request Forgery

If browser authentication uses cookies, CSRF protection must be configured appropriately.

The implementation must consider the interaction between:

- Cookies
- SameSite policy
- CSRF tokens
- CORS
- Authentication endpoints

CSRF protection must match the final authentication architecture rather than being enabled or disabled without considering how authentication credentials are transported.

---

### 17.17 CORS

Cross-Origin Resource Sharing must be explicitly configured.

Production CORS should allow only trusted frontend origins.

The backend must not use an unrestricted wildcard configuration for authenticated production APIs unless the security implications have been deliberately evaluated.

Development and production configurations may differ.

---

### 17.18 Rate Limiting

Rate limiting should protect sensitive or expensive endpoints.

Potential targets include:

- Login
- Password reset
- Registration
- File upload
- Search
- Report generation
- Public API endpoints
- External webhook endpoints

Rate limiting helps reduce:

- Brute-force attacks
- Abuse
- Accidental overload
- Resource exhaustion

Redis may eventually support distributed rate limiting.

---

### 17.19 File Upload Security

File uploads represent a significant attack surface.

The backend must validate:

- File size
- File type
- Content type
- Filename
- File extension
- Storage path/key

Where appropriate, uploaded files should be scanned for malware.

User-controlled filenames must not be used directly to construct filesystem paths.

Files should be stored using generated identifiers or controlled object-storage keys.

---

### 17.20 Object Storage Security

Object storage must not expose sensitive files publicly by default.

Access should be controlled through the application.

Short-lived signed URLs may be used when direct browser access to an object is appropriate.

Authorization must occur before generating the URL.

The URL itself must not become a permanent authorization mechanism.

---

### 17.21 Secrets Management

Sensitive configuration must never be committed to Git.

Examples include:

- JWT signing secrets
- Database passwords
- AWS credentials
- Stripe secrets
- Email provider API keys
- Kafka credentials
- Redis credentials

Development may use environment variables or local secret configuration.

Production should use an appropriate secret-management mechanism.

---

### 17.22 Security Headers

The production application should use appropriate HTTP security headers.

Potential protections include:

- Content Security Policy
- X-Content-Type-Options
- Referrer-Policy
- Frame protection
- Strict Transport Security

The exact configuration will be finalized during deployment and security hardening.

---

### 17.23 HTTPS

Production communication must use HTTPS.

Sensitive credentials, tokens, and business data must not be transmitted over unencrypted HTTP.

HTTP-to-HTTPS redirection may be configured at the infrastructure layer.

---

### 17.24 Logging Security

Application logs must not contain sensitive credentials.

The system must avoid logging:

- Passwords
- Password-reset tokens
- JWT tokens
- API keys
- Payment credentials
- Sensitive personal information unnecessarily

Logs should contain enough context for debugging and monitoring without becoming a source of data leakage.

---

### 17.25 Audit Security

Security-sensitive actions should produce audit events.

Examples include:

- Login
- Failed login
- Password change
- Password reset
- Account lock
- Role assignment
- Permission changes
- Tenant membership changes
- Platform administration
- Sensitive resource access
- File access where appropriate

Audit records must themselves be protected from unauthorized modification.

---

### 17.26 WebSocket Security

If WebSockets are introduced, authentication and authorization must apply to the WebSocket connection and relevant messages.

A user must only receive real-time events for resources they are authorized to access.

Tenant context must be preserved.

A WebSocket must not provide a backdoor around REST/API authorization.

---

### 17.27 Kafka Security

When Kafka is introduced, event messages must respect tenant boundaries.

Events should contain enough context for consumers to process them correctly.

Consumers must not assume that an event received from Kafka is automatically safe to process without validation.

Kafka topics and consumer permissions must be configured appropriately.

Sensitive information should not be placed into events unnecessarily.

---

### 17.28 Webhook Security

External webhooks, such as payment-provider webhooks, must be verified.

The system should validate:

- Signature/authentication
- Event format
- Event identity
- Timestamp/replay protections where supported
- Idempotency

Webhook endpoints must not trust arbitrary HTTP requests simply because they target a known URL.

---

### 17.29 Dependency Security

Third-party dependencies must be monitored for known vulnerabilities.

This includes:

- Java dependencies
- Spring dependencies
- Angular dependencies
- Node packages
- Docker images
- Infrastructure components

Dependency versions should be kept reasonably current.

Security updates should be evaluated and applied appropriately.

---

### 17.30 Security Testing

Security must be tested explicitly.

Testing should include:

- Authentication tests
- Authorization tests
- Tenant-isolation tests
- Customer-isolation tests
- IDOR tests
- Validation tests
- File-upload security tests
- Rate-limit tests
- Webhook verification tests
- CORS tests
- Session/token tests

Particularly important tests include:

```text id="x1q5v7"
Tenant A user
      ↓
Attempt to access Tenant B resource
      ↓
ACCESS DENIED
```

and:

```text id="p4c8z2"
Customer A user
      ↓
Attempt to access Customer B resource
      ↓
ACCESS DENIED
```

---

### 17.31 Threat Model

The initial threat model should consider at least:

#### Unauthorized users

Attempt to access protected resources without authentication.

#### Authenticated but unauthorized users

Attempt to access resources or actions beyond their permissions.

#### Cross-tenant attackers

Attempt to access another tenant's data by manipulating IDs, filters, requests, files, or events.

#### Malicious customer users

Attempt to access another customer's information within the same tenant.

#### Credential attackers

Attempt brute-force or credential-stuffing attacks against authentication endpoints.

#### Malicious file uploads

Attempt to upload executable, malicious, oversized, or otherwise dangerous content.

#### API abuse

Attempt to exhaust resources through repeated requests, searches, uploads, or report generation.

#### Compromised integrations

Attempt to exploit webhook or external integration endpoints.

#### Insider misuse

Authorized users intentionally or accidentally access information beyond their legitimate responsibilities.

---

### 17.32 Security Failure Behavior

Security failures should fail closed.

Examples:

```text id="m3c7x1"
Missing tenant context
        ↓
Reject request

Invalid token
        ↓
Reject request

Insufficient permission
        ↓
Reject request

Unauthorized resource
        ↓
Reject request
```

The application must not fall back to broad access when security information is missing.

---

### 17.33 Business Rules

Initial rules:

1. Authentication is required for protected operations.
2. Passwords must never be stored or logged in plaintext.
3. Passwords must use strong password hashing.
4. JWT access tokens must have limited lifetimes.
5. Sensitive information must not be unnecessarily stored in JWT claims.
6. Authorization must be enforced by the backend.
7. Authorization must include resource-level access checks.
8. Tenant context must come from authenticated identity and membership.
9. Cross-tenant access must be explicitly prevented.
10. Customer users must be isolated from other customers within the same tenant.
11. Platform administration must remain separate from tenant administration.
12. All client-controlled input must be treated as untrusted.
13. Database access must protect against SQL injection.
14. User-generated content must be handled safely to prevent XSS.
15. CSRF protection must match the chosen authentication architecture.
16. Production CORS must allow only trusted origins.
17. Sensitive endpoints should be protected by rate limiting.
18. File uploads require server-side security validation.
19. Object-storage files must not be publicly accessible by default.
20. Secrets must never be committed to source control.
21. Production communication must use HTTPS.
22. Sensitive credentials and tokens must not be written to logs.
23. Security-sensitive actions should be audited.
24. WebSockets must enforce authentication and authorization.
25. Kafka events must respect tenant boundaries.
26. External webhooks must be authenticated and protected against replay/duplication where appropriate.
27. Dependencies must be monitored for security vulnerabilities.
28. Security controls must be covered by automated tests.
29. Security failures must fail closed.
30. The application must be designed and tested against cross-tenant and cross-customer data access.

## 18. Observability, Logging, Monitoring & Operations

WorkFlow SaaS must provide sufficient operational visibility to detect failures, diagnose problems, monitor performance, and understand system behavior in development and production.

Observability is treated as a cross-cutting concern rather than functionality belonging to a single business module.

The initial architecture will use application logs, metrics, health checks, and correlation identifiers.

The production architecture will progressively add centralized monitoring, distributed tracing, dashboards, alerting, and operational automation.

---

### 18.1 Observability Goals

The observability system should answer questions such as:

- Is the application running?
- Is the database available?
- Are requests failing?
- Which API endpoints are slow?
- Are background jobs failing?
- Are Kafka events being processed?
- Are notifications being delivered?
- Are scheduled maintenance jobs running?
- Is Redis available?
- Is storage available?
- Are users experiencing increased errors?
- Which request caused a particular failure?
- Which tenant or operation was affected?

Observability must provide enough information to diagnose incidents without exposing sensitive information.

---

### 18.2 Three Pillars of Observability

The system will use the three standard observability categories:

```text id="q7n2v4"
Logs
  ↓
What happened?

Metrics
  ↓
How much / how often / how fast?

Traces
  ↓
Where did the operation travel?
```

These should complement each other rather than replace one another.

---

### 18.3 Application Logging

The backend should use structured application logging.

Logs should contain useful contextual information such as:

- Timestamp
- Log level
- Application/module
- Environment
- Message
- Correlation ID
- Request ID where applicable
- Operation
- Error information
- Relevant entity identifiers where appropriate
- Tenant context where safe

Example conceptual log:

```text id="r4k8m1"
INFO
tenant=tenant-123
correlationId=abc-456
operation=WORK_ORDER_ASSIGN
workOrderId=789
message="Work order assigned"
```

Logs should be machine-readable where practical so they can later be indexed and searched.

---

### 18.4 Log Levels

The application should use appropriate log levels.

#### ERROR

Unexpected failures requiring investigation.

Examples:

- Database failure
- External service failure
- Unhandled exception
- Failed background processing

#### WARN

Unexpected or potentially problematic situations that do not necessarily represent application failure.

Examples:

- Retry triggered
- Approaching resource limit
- External service temporarily unavailable
- Suspicious repeated authentication failures

#### INFO

Important normal application events.

Examples:

- Application startup
- Application shutdown
- Major background job execution
- Important business operations where operational visibility is useful

#### DEBUG

Detailed diagnostic information useful during development or troubleshooting.

Debug logging should not expose sensitive data.

---

### 18.5 Sensitive Data in Logs

Logs must not expose sensitive information unnecessarily.

The application must avoid logging:

- Passwords
- Password-reset tokens
- JWT access tokens
- Refresh tokens
- API keys
- Database credentials
- Payment credentials
- Secrets
- Sensitive personal information unnecessarily

Request bodies should not automatically be logged in production.

File contents must never be logged.

---

### 18.6 Correlation IDs

Each API request should have a correlation identifier.

Conceptually:

```text id="v8m3x6"
HTTP Request
     ↓
Correlation ID
     ↓
Controller
     ↓
Service
     ↓
Database
     ↓
Event / Background Job
```

The correlation ID allows related logs to be connected during troubleshooting.

If a request produces asynchronous work, the relevant correlation information should be propagated where appropriate.

---

### 18.7 Tenant Context in Logs

Tenant context may be included in logs when it improves operational troubleshooting.

For example:

```text id="c2f7p9"
tenantId=tenant-123
workOrderId=456
operation=WORK_ORDER_COMPLETE
```

Tenant identifiers must not be used as authorization mechanisms merely because they appear in logs.

Logging tenant information must also respect privacy and data-minimization requirements.

---

### 18.8 Error Handling and Logging

Errors should be logged at the appropriate layer.

The system should avoid logging the same exception repeatedly at every layer.

A useful pattern is:

```text id="a6w1k5"
Exception occurs
      ↓
Business/application layer adds context
      ↓
Global error handler produces API response
      ↓
Failure is logged once with correlation ID
```

API responses should provide useful error information without exposing internal implementation details, stack traces, database information, or secrets.

---

### 18.9 API Metrics

The backend should collect metrics such as:

- Request count
- Response count
- Error count
- HTTP status distribution
- Request latency
- Requests by endpoint
- Requests by method
- Authentication failures
- Rate-limit events

Latency should eventually be analyzed using percentiles such as:

- p50
- p95
- p99

Average latency alone is insufficient for understanding user-facing performance.

---

### 18.10 Business Metrics

Operational business metrics should also be monitored.

Examples include:

- Service Requests created
- Service Requests approved/rejected
- Work Orders created
- Work Orders completed
- Work Orders failed
- Work Orders cancelled
- Work Orders overdue
- Work Orders currently in progress
- Maintenance tasks due
- Maintenance tasks overdue
- Inventory low-stock events
- Invoices issued
- Invoices overdue
- Notification failures

Business metrics should remain tenant-aware where appropriate.

---

### 18.11 Infrastructure Metrics

Infrastructure monitoring should eventually include:

- CPU utilization
- Memory usage
- Disk usage
- Network usage
- JVM memory
- JVM garbage collection
- Thread counts
- Database connections
- Database query performance
- Redis health
- Kafka health
- Object-storage operations
- Container health

These metrics help distinguish application problems from infrastructure problems.

---

### 18.12 Spring Boot Actuator

Spring Boot Actuator will provide application-level operational endpoints.

Potential capabilities include:

- Health checks
- Metrics
- Application information
- Environment diagnostics where safely exposed

Actuator endpoints must not be publicly exposed without appropriate protection.

Sensitive management endpoints should require authentication and authorization or remain inaccessible externally.

---

### 18.13 Health Checks

The application should distinguish between different health states.

#### Liveness

Answers:

> Is the application process running?

A liveness failure may indicate that the application should be restarted.

#### Readiness

Answers:

> Is the application ready to receive traffic?

Readiness may depend on required infrastructure such as:

- PostgreSQL
- Redis where required
- Kafka where required
- Other mandatory dependencies

The exact readiness requirements will depend on deployment architecture.

---

### 18.14 Database Monitoring

PostgreSQL should be monitored for:

- Connection count
- Connection pool usage
- Query latency
- Slow queries
- Locks
- Deadlocks
- Transaction duration
- Database size
- Index usage
- Failed queries
- Replication status if replication is introduced
- Backup status

Database monitoring should help identify performance problems before they become application-wide failures.

---

### 18.15 Redis Monitoring

When Redis is introduced, monitoring should include:

- Availability
- Memory usage
- Connection count
- Command latency
- Cache hit/miss behavior
- Evictions
- Errors

Redis must not become a hidden single point of failure for functionality that can safely operate without it.

The application should define appropriate behavior when Redis is unavailable.

---

### 18.16 Kafka Monitoring

When Kafka is introduced, monitoring should include:

- Producer failures
- Consumer failures
- Consumer lag
- Processing latency
- Retry counts
- Dead-letter events
- Topic health
- Broker availability

A growing consumer lag may indicate that events are being produced faster than they can be processed.

---

### 18.17 Background Job Monitoring

Background jobs are important to WorkFlow SaaS because they may handle:

- Maintenance-plan evaluation
- Maintenance Work Order generation
- Notifications
- Email delivery
- Report generation
- Subscription processing
- Cleanup tasks
- Retry processing

Each important job should provide operational visibility.

Monitoring should include:

- Job execution count
- Success count
- Failure count
- Duration
- Last successful execution
- Last failure
- Retry count

---

### 18.18 Scheduled Job Safety

Scheduled jobs must be designed so that retries do not create duplicate business operations.

For example, a maintenance job must not create multiple Work Orders for the same maintenance occurrence simply because it executed twice.

Idempotency and unique business constraints should be used where appropriate.

---

### 18.19 Notification Monitoring

Notification delivery should be observable separately from notification creation.

The system should distinguish:

```text id="t3y8q2"
Notification Created
        ↓
Queued
        ↓
Delivery Attempted
        ↓
Delivered / Failed
        ↓
Retry if appropriate
```

Monitoring should identify:

- Delivery failures
- Retry volume
- Email provider failures
- Undelivered notifications
- Processing delays

---

### 18.20 External Service Monitoring

External dependencies may include:

- Email provider
- Payment provider
- AWS services
- Object storage
- Authentication-related services
- Future third-party integrations

The system should monitor:

- Availability
- Response latency
- Error rate
- Timeout rate
- Retry volume

External failures should not unnecessarily expose internal implementation details to users.

---

### 18.21 Distributed Tracing

Distributed tracing may be introduced as the system grows.

Tracing is especially useful when a request crosses multiple components:

```text id="n5c1r7"
Angular
  ↓
Spring Boot API
  ↓
PostgreSQL
  ↓
Kafka
  ↓
Notification Worker
  ↓
Email Provider
```

A trace allows the complete operation to be investigated across these boundaries.

Tracing should be implemented when the system has enough distributed behavior to justify the additional complexity.

---

### 18.22 Performance Monitoring

Performance monitoring should focus on real user-impacting operations.

Potential areas include:

- API latency
- Database queries
- Dashboard queries
- Search
- Reports
- File uploads/downloads
- Authentication
- Work Order operations
- Large exports
- Background jobs

Performance problems should be investigated using measurements rather than assumptions.

---

### 18.23 Alerting

Production monitoring should generate alerts for important failures.

Potential alerts include:

- Application unavailable
- High API error rate
- High API latency
- Database unavailable
- Database connection exhaustion
- High JVM memory usage
- High CPU usage
- Disk nearly full
- Kafka consumer lag
- Background job failures
- Notification delivery failures
- Payment processing failures
- Backup failures
- Certificate expiration
- Security-related anomalies

Alerts should be actionable rather than generating excessive noise.

---

### 18.24 Alert Severity

Alerts may eventually be classified by severity.

Example:

```text id="u9d4f2"
CRITICAL
Production unavailable
Database unavailable

HIGH
Very high error rate
Payment processing failure

MEDIUM
Background job failures
Increasing consumer lag

LOW
Non-critical warning
Capacity approaching threshold
```

Severity definitions should be finalized during deployment and operations design.

---

### 18.25 Monitoring Dashboard

The production environment should eventually provide dashboards covering:

#### Application

- Request rate
- Error rate
- Latency
- HTTP status distribution

#### JVM

- Heap
- Non-heap memory
- Garbage collection
- Threads

#### Database

- Connections
- Query performance
- Locks
- Errors

#### Messaging

- Kafka throughput
- Consumer lag
- Failed messages

#### Infrastructure

- CPU
- Memory
- Disk
- Network

#### Business

- Work Orders
- Service Requests
- Maintenance
- Inventory
- Invoices
- Notifications

Prometheus and Grafana are planned technologies for this monitoring layer.

---

### 18.26 Incident Investigation

When an incident occurs, operators should be able to follow a path such as:

```text id="f6k2w9"
Alert
 ↓
Metric
 ↓
Correlation ID / Trace
 ↓
Application Logs
 ↓
Database / Infrastructure
 ↓
Root Cause
 ↓
Resolution
```

The architecture should make this path possible without requiring direct inspection of production data whenever avoidable.

---

### 18.27 Backup and Recovery Monitoring

Database backups are part of operational reliability.

The system should monitor:

- Backup execution
- Backup success/failure
- Backup age
- Storage availability
- Retention
- Restore-test results

A backup that has never been successfully restored should not be treated as fully verified.

Recovery procedures should eventually be documented and tested.

---

### 18.28 Data Retention

Operational data should have appropriate retention policies.

Different categories may require different retention periods:

- Application logs
- Audit events
- Metrics
- Traces
- Notifications
- Uploaded files
- Database backups

Retention policies should balance operational requirements, storage cost, security, and applicable legal/business requirements.

---

### 18.29 Production Configuration

Production configuration must differ appropriately from development configuration.

Production should generally:

- Disable verbose debugging
- Protect management endpoints
- Use secure secrets
- Use HTTPS
- Restrict CORS
- Enable appropriate monitoring
- Enable structured logging
- Configure backups
- Configure alerting
- Avoid sensitive data in logs

---

### 18.30 Operational Documentation

The project should maintain operational documentation covering:

- Application startup
- Application shutdown
- Database migrations
- Backup and restore
- Deployment
- Rollback
- Configuration
- Secret management
- Monitoring
- Alert response
- Common failures
- Incident response

This documentation should evolve together with the production architecture.

---

### 18.31 Business Rules

Initial observability and operations rules:

1. Production systems must provide application health information.
2. Application logs must be structured and searchable.
3. Sensitive credentials and tokens must never be logged.
4. API requests should have correlation identifiers.
5. Errors must provide sufficient diagnostic context without exposing sensitive implementation details.
6. API performance must be measurable.
7. Important business operations should have appropriate operational metrics.
8. Infrastructure resources must be monitored.
9. Database health and performance must be monitored.
10. Redis health must be monitored when Redis is introduced.
11. Kafka health and consumer lag must be monitored when Kafka is introduced.
12. Background jobs must expose execution and failure information.
13. Important background operations should be idempotent where retries are possible.
14. Notification creation and notification delivery should be observable separately.
15. External service failures must be detectable.
16. Actuator management endpoints must be appropriately protected.
17. Liveness and readiness must be distinguished.
18. Production alerts must be actionable.
19. Monitoring dashboards should combine application, infrastructure, and relevant business metrics.
20. Backup execution and restore capability must be monitored.
21. Operational data must follow defined retention policies.
22. Production configuration must not expose development-level debugging or secrets.
23. Incident investigation must be possible through logs, metrics, and traces where applicable.
24. Operational procedures must be documented and maintained.

## 19. Testing Strategy & Quality Assurance

Testing is a core part of the WorkFlow SaaS development process.

The testing strategy must verify not only that individual methods work, but also that business rules, authorization boundaries, tenant isolation, data integrity, integrations, and complete user workflows behave correctly.

The project will use multiple levels of testing rather than relying exclusively on unit tests or end-to-end tests.

---

### 19.1 Testing Goals

The testing strategy should provide confidence that:

- Business rules are implemented correctly.
- Invalid operations are rejected.
- State transitions are enforced.
- Tenant isolation cannot be bypassed.
- Customer isolation is enforced.
- Authorization works correctly.
- Database persistence behaves correctly.
- API contracts are consistent.
- Background processing is reliable.
- External integrations fail safely.
- Important workflows work end-to-end.
- Changes do not unintentionally break existing functionality.

---

### 19.2 Testing Pyramid

The project will generally follow a testing pyramid:

```text
                 E2E Tests
              /-------------\
             /               \
        Integration Tests
        /---------------------\
       /                       \
        Unit / Domain Tests
      /-------------------------\
```

The majority of tests should be fast unit tests.

Integration tests should verify interactions between application components.

End-to-end tests should cover important user journeys rather than every possible implementation detail.

---

### 19.3 Unit Tests

Unit tests verify individual pieces of business logic in isolation.

Potential targets include:

- Domain logic
- State transitions
- Validators
- Business services
- Permission evaluation
- Scheduling rules
- Inventory calculations
- Invoice calculations
- Maintenance recurrence logic
- Notification rules

Examples:

```text id="a7k2m4"
Work Order:
SCHEDULED → ASSIGNED
       ↓
Allowed

COMPLETED → IN_PROGRESS
       ↓
Rejected
```

Unit tests should verify both valid and invalid operations.

---

### 19.4 Domain State-Machine Testing

State transitions are critical business rules.

Tests must verify that only permitted transitions are accepted.

For example:

```text id="p4v8n1"
NEW
 ↓
UNDER_REVIEW
 ↓
APPROVED
 ↓
CONVERTED_TO_WORK_ORDER
```

Tests should also verify invalid transitions such as:

```text id="c6x3r9"
COMPLETED → IN_PROGRESS
CANCELLED → ASSIGNED
REJECTED → IN_PROGRESS
```

The exact valid transition matrix should be maintained as part of the implementation.

---

### 19.5 Authorization Testing

Authorization must be tested independently from authentication.

Tests should verify that:

- Users with required permissions can perform authorized operations.
- Users without permissions are rejected.
- Tenant administrators cannot automatically access platform administration.
- Customer users cannot perform internal-only operations.
- Technicians cannot perform manager-only operations.
- Users cannot access resources outside their allowed scope.

A successful authentication must never be treated as proof that an operation is authorized.

---

### 19.6 Tenant Isolation Testing

Tenant isolation is a critical security requirement and must have dedicated automated tests.

Example:

```text id="r8m2w5"
Tenant A User
     ↓
Request for Tenant B Work Order
     ↓
403 / 404 according to API security design
```

Tests should cover:

- GET
- POST
- PUT/PATCH
- DELETE
- Search
- Filtering
- Sorting
- Pagination
- Reports
- Exports
- Files
- Notifications
- Background jobs
- Events
- Cache

The system must not leak another tenant's data through any of these mechanisms.

---

### 19.7 Customer Isolation Testing

Customer-facing authorization requires a second isolation boundary.

Tests should verify:

```text id="m5q9t2"
Customer A User
      ↓
Customer B Work Order
      ↓
Access Denied
```

Tests should cover resources such as:

- Service Requests
- Work Orders
- Locations
- Equipment
- Invoices
- Documents
- Notifications
- Maintenance history

---

### 19.8 API Tests

API tests should verify the behavior of REST endpoints.

Tests should cover:

- HTTP methods
- Request validation
- Authentication
- Authorization
- Response status codes
- Response structures
- Error responses
- Pagination
- Filtering
- Sorting
- State-changing operations
- Idempotency where applicable

Important status codes should be tested explicitly, including:

```text id="w1c7h4"
200
201
204
400
401
403
404
409
422
429
500
```

---

### 19.9 Validation Testing

Backend validation must be tested even when equivalent validation exists in Angular.

Examples include:

- Required fields
- Maximum lengths
- Invalid dates
- Invalid quantities
- Invalid email addresses
- Invalid state transitions
- Invalid references
- Negative inventory quantities
- Invalid scheduling periods

Tests should verify that malformed or invalid input is rejected consistently.

---

### 19.10 Persistence and Repository Tests

Database-related tests should verify:

- Entity persistence
- Relationships
- Foreign keys
- Unique constraints
- Query behavior
- Tenant filtering
- Pagination
- Sorting
- Transaction behavior
- Optimistic locking
- Soft deletion where applicable

Queries involving tenant-scoped entities should receive particular attention.

---

### 19.11 Integration Tests

Integration tests verify interactions between multiple application components.

Examples:

```text id="j3f8s6"
Controller
   ↓
Application Service
   ↓
Repository
   ↓
PostgreSQL
```

Integration tests should verify that these layers work together correctly.

Important integrations include:

- Spring Security
- PostgreSQL
- Flyway
- Redis
- Kafka
- Object storage
- External payment provider
- Email provider

Not every integration must be enabled in every test.

---

### 19.12 Testcontainers

Testcontainers should be used for integration tests that depend on infrastructure.

Potential containers include:

- PostgreSQL
- Redis
- Kafka

This allows tests to run against realistic infrastructure rather than relying exclusively on mocks.

Example:

```text id="n4x7p2"
Integration Test
      ↓
Testcontainers
      ↓
Real PostgreSQL
      ↓
Application
```

This is particularly useful for detecting differences between mocked behavior and real database behavior.

---

### 19.13 Flyway Migration Testing

Database migrations must be tested as part of the application lifecycle.

Tests should verify that:

- Migrations execute successfully.
- Database schema is created correctly.
- Migrations work in the expected order.
- Existing data remains compatible with schema changes where applicable.
- Application startup works against the migrated database.

Migration failures must prevent an invalid application/database combination from being treated as healthy.

---

### 19.14 Transaction Testing

Transactions are important for operations that modify multiple records.

Tests should verify atomic behavior.

For example:

```text id="v6k1r8"
Create Work Order
   +
Create Assignment
   +
Create History
        ↓
All succeed
```

If a required operation fails:

```text id="q9m4c2"
Create Work Order
   +
Create Assignment
   X
Failure
        ↓
Transaction rolled back
```

The exact transactional boundaries will be defined during implementation.

---

### 19.15 Concurrency Testing

Certain operations may be executed concurrently.

Tests should eventually cover scenarios such as:

- Two users assigning the same Work Order.
- Two users scheduling the same technician.
- Two users consuming the same inventory.
- Concurrent invoice updates.
- Concurrent Work Order state changes.

Optimistic locking or other concurrency controls must prevent invalid final states.

---

### 19.16 Inventory Testing

Inventory requires dedicated tests because quantities must remain consistent.

Tests should cover:

- Receiving stock
- Transfers
- Consumption
- Returns
- Adjustments
- Damaged stock
- Lost stock
- Low-stock thresholds
- Negative-stock prevention
- Concurrent consumption

Example:

```text id="b2w7k5"
Available Stock = 10

Consume 3
   ↓
Available Stock = 7

Consume 8
   ↓
Rejected
```

Historical stock movements must remain consistent with the resulting inventory state.

---

### 19.17 Scheduling Testing

Scheduling must verify:

- Valid schedules
- Invalid time ranges
- Technician availability
- Overlapping assignments
- Required skills
- Required certifications
- Expired certifications
- Leave/unavailability
- Rescheduling
- Schedule history
- Time zones

Example:

```text id="z5p3n8"
Technician
09:00 ───── 11:00
        +
10:00 ───── 12:00
        ↓
Conflict
```

The final scheduling rules may evolve as the scheduling module becomes more sophisticated.

---

### 19.18 Maintenance Testing

Maintenance tests should verify:

- Recurrence calculations
- Due dates
- Overdue detection
- Work Order generation
- Required skills
- Required certifications
- Required parts
- Skipping
- Rescheduling
- Completion
- Duplicate Work Order prevention

A maintenance job executing twice must not accidentally create duplicate Work Orders for the same occurrence.

---

### 19.19 Billing and Invoice Testing

Billing tests should verify:

- Invoice creation
- Line-item calculations
- Taxes
- Discounts
- Totals
- Currency
- Status transitions
- Payment state changes
- Historical pricing
- Work Order billable items

Financial calculations should be deterministic and tested with boundary cases.

Examples include:

- Zero-value lines
- Decimal quantities where supported
- Discounts
- Taxes
- Rounding
- Large values
- Currency-specific precision

---

### 19.20 File Upload Testing

File handling should be tested for:

- Valid files
- Unsupported file types
- Oversized files
- Invalid metadata
- Malicious filenames
- Unauthorized uploads
- Unauthorized downloads
- Cross-tenant access
- Deleted/archived files
- Signed URL authorization

A user must never obtain a file merely by knowing or guessing its storage key.

---

### 19.21 Notification Testing

Notification tests should verify:

- Correct recipients
- Correct tenant
- Correct event
- Notification creation
- Read/unread state
- Delivery status
- Retry behavior
- Duplicate prevention
- User preferences

Example:

```text id="k8v2m6"
Work Order Assigned
        ↓
Technician receives notification
        ↓
Unrelated customer does not receive notification
```

---

### 19.22 Event and Kafka Testing

When Kafka is introduced, tests should verify:

- Event creation
- Event serialization
- Correct tenant context
- Consumer processing
- Retry behavior
- Duplicate event handling
- Dead-letter behavior
- Consumer failure recovery

Consumers should be designed to tolerate duplicate processing where appropriate.

---

### 19.23 Webhook Testing

External webhooks must be tested for:

- Valid signatures
- Invalid signatures
- Duplicate events
- Replay attempts where applicable
- Malformed payloads
- Unknown event types
- Processing failures
- Retry behavior

Webhook tests must verify that unauthenticated or invalid requests cannot modify business state.

---

### 19.24 Security Testing

Security testing should include:

- Authentication
- Authorization
- Password security
- Token expiration
- Token invalidation where applicable
- IDOR/BOLA
- Tenant isolation
- Customer isolation
- CSRF where applicable
- CORS
- Rate limiting
- Input validation
- File security
- Webhook authentication
- Sensitive-data exposure

Security tests should be part of the automated test suite rather than performed only manually.

---

### 19.25 End-to-End Testing

End-to-end tests should validate complete business workflows.

Important workflows include:

#### Reactive Service Workflow

```text id="s4n7x1"
Customer/Requester
      ↓
Service Request
      ↓
Manager Review
      ↓
Approval
      ↓
Work Order
      ↓
Assignment
      ↓
Scheduling
      ↓
Technician
      ↓
Completion
      ↓
Customer Confirmation
      ↓
Invoice
```

#### Preventive Maintenance Workflow

```text id="h2q8m5"
Maintenance Plan
      ↓
Due
      ↓
Work Order Generated
      ↓
Assignment
      ↓
Scheduling
      ↓
Technician Work
      ↓
Completion
      ↓
Maintenance History
```

---

### 19.26 Frontend Testing

The Angular application should have tests for:

- Components
- Services
- Forms
- Validation
- Route guards
- HTTP behavior
- Authentication state
- Error handling
- Important user interactions

Frontend tests should verify UI behavior but must not be treated as a substitute for backend security testing.

---

### 19.27 API Contract Testing

The API contract should be tested against the documented API specification.

Tests should help detect:

- Missing fields
- Unexpected response structures
- Incorrect status codes
- Breaking changes
- Incorrect validation behavior

OpenAPI documentation should remain aligned with the actual API.

---

### 19.28 Regression Testing

Every significant change should preserve existing behavior unless a deliberate breaking change is introduced.

The automated test suite should be executed during development and CI.

Regression testing should focus particularly on:

- Authentication
- Tenant isolation
- State transitions
- Financial calculations
- Inventory
- Scheduling
- API contracts

---

### 19.29 Test Data

Test data should be deterministic and isolated.

Tests should not depend on manually created production-like data.

Test fixtures should clearly represent scenarios such as:

- Multiple tenants
- Multiple customers
- Multiple users
- Different roles
- Technicians
- Work Orders
- Equipment
- Inventory
- Invoices

Multi-tenant test data is particularly important for detecting isolation failures.

---

### 19.30 Test Environment Isolation

Tests should not accidentally interact with production systems.

External services should use:

- Test environments
- Local containers
- Mocks
- Stubs
- Dedicated credentials

Production credentials must never be used by automated tests.

---

### 19.31 CI Testing

GitHub Actions will eventually execute automated tests during the CI pipeline.

A typical pipeline may eventually include:

```text id="x6r1c9"
Push / Pull Request
        ↓
Compile
        ↓
Unit Tests
        ↓
Integration Tests
        ↓
Security Checks
        ↓
Build
        ↓
Docker Image
```

Additional deployment stages will be added as the infrastructure evolves.

---

### 19.32 Code Quality

Testing is complemented by code-quality checks.

The project should eventually include automated checks for:

- Compilation
- Formatting
- Static analysis
- Dependency vulnerabilities
- Test coverage
- Build consistency

The exact tools will be selected during implementation.

---

### 19.33 Test Coverage

Code coverage may be used as a measurement tool but should not become the sole definition of test quality.

High coverage does not guarantee correct business behavior.

Priority should be given to testing:

- Security boundaries
- Business rules
- State transitions
- Financial calculations
- Data integrity
- Tenant isolation
- Critical workflows

---

### 19.34 Failure Testing

The application should also test failure scenarios.

Examples:

- Database unavailable
- Redis unavailable
- Kafka unavailable
- Email provider unavailable
- Payment provider unavailable
- Storage unavailable
- Timeout
- Duplicate event
- Concurrent update
- Invalid external response

The objective is to verify that failures result in controlled behavior rather than corrupted state or security violations.

---

### 19.35 Quality Gates

Before code is considered ready for production, appropriate quality gates should be satisfied.

Potential gates include:

- Successful compilation
- Unit tests passing
- Integration tests passing
- Security tests passing
- No known critical dependency vulnerabilities
- API contract validation
- Migration validation
- Required code-quality checks
- Successful build
- Successful deployment verification

The exact CI/CD gates will be finalized during infrastructure implementation.

---

### 19.36 Business Rules

Initial testing rules:

1. Business logic must be tested independently of controllers where practical.
2. Valid and invalid state transitions must both be tested.
3. Authorization must have dedicated automated tests.
4. Tenant isolation must have dedicated automated tests.
5. Customer isolation must have dedicated automated tests.
6. API endpoints must test authentication, authorization, validation, and response behavior.
7. Database interactions must be tested against realistic persistence behavior.
8. Testcontainers should be used for important infrastructure integrations.
9. Transactions must be tested for atomicity where appropriate.
10. Concurrency-sensitive operations must be tested.
11. Inventory operations must have dedicated tests.
12. Scheduling conflicts must be tested.
13. Maintenance recurrence and duplicate prevention must be tested.
14. Financial calculations must be tested with boundary and rounding cases.
15. File authorization and cross-tenant file access must be tested.
16. Notifications must be tested for recipient and tenant correctness.
17. Kafka consumers must be tested for duplicate and failure scenarios when Kafka is introduced.
18. External webhooks must be tested for authentication and idempotency.
19. End-to-end tests must cover critical business workflows.
20. Frontend tests must not replace backend security tests.
21. API contracts should be tested against documented behavior.
22. Automated tests should run in CI.
23. Test environments must never use production credentials or accidentally modify production data.
24. Code coverage should support quality measurement but must not replace meaningful tests.
25. Critical security and business rules must remain protected by regression tests.
26. Failure scenarios should be tested for important infrastructure and external dependencies.
27. Production readiness should require passing defined quality gates.

## 20. Deployment, CI/CD & Infrastructure Architecture

WorkFlow SaaS will use an automated deployment and infrastructure strategy that supports reliable development, testing, staging, and production environments.

The initial deployment architecture should remain simple enough for a modular monolith while providing a clear path toward production-grade infrastructure.

The system will use containerization, automated CI/CD, managed infrastructure where practical, and infrastructure configuration that can be reproduced consistently.

---

### 20.1 Deployment Goals

The deployment architecture should provide:

- Reproducible application builds
- Automated testing
- Consistent environments
- Secure configuration
- Controlled deployments
- Database migration management
- Rollback capability
- Health monitoring
- Production observability
- Minimal manual deployment work

---

### 20.2 Environments

The project should distinguish between environments.

Initial environments:

```text id="m8v3q1"
Development
     ↓
Testing / CI
     ↓
Staging
     ↓
Production
```

### Development

Used for local implementation.

Typical components:

- Spring Boot
- Angular
- PostgreSQL
- Redis when required
- Kafka when required
- Local object storage or development S3 configuration

### CI / Test

Used by GitHub Actions to compile and test the application.

Infrastructure dependencies may be provided through Testcontainers.

### Staging

Used to validate production-like deployments before production release.

Staging should use isolated resources and credentials.

### Production

The real customer-facing environment.

Production must use independent credentials, databases, storage, monitoring, and other resources.

---

### 20.3 Environment Isolation

Environments must not accidentally share sensitive resources.

For example:

```text id="q4n7w2"
Development
    └── Development Database

Staging
    └── Staging Database

Production
    └── Production Database
```

Production credentials must never be used in development or CI.

Development data must never accidentally be written into production.

---

### 20.4 Containerization

Docker will be used to package application components consistently.

Potential containers include:

- Spring Boot backend
- Angular frontend
- PostgreSQL for local development
- Redis
- Kafka

The exact production architecture may use managed services instead of running every component inside application-managed containers.

Containerization should primarily provide reproducibility and deployment consistency.

---

### 20.5 Docker Images

Production application images should:

- Use a minimal appropriate base image
- Run as a non-root user where practical
- Avoid unnecessary packages
- Use explicit versions
- Keep secrets outside the image
- Expose only required ports
- Provide health-check behavior where appropriate

Images should be reproducible and traceable to a source-code version.

---

### 20.6 Image Versioning

Production images should not rely exclusively on mutable tags such as:

```text id="s3k9x5"
latest
```

Images should have identifiable versions.

Possible identifiers include:

- Git commit SHA
- Release version
- Semantic version
- Build number

This makes deployments traceable and supports rollback.

---

### 20.7 GitHub Actions

GitHub Actions will provide the initial CI/CD platform.

The pipeline should eventually automate:

```text id="p7m2c8"
Git Push / Pull Request
        ↓
Checkout
        ↓
Build
        ↓
Unit Tests
        ↓
Integration Tests
        ↓
Security / Quality Checks
        ↓
Package
        ↓
Build Docker Image
        ↓
Publish Image
        ↓
Deploy
        ↓
Health Check
```

The exact pipeline will evolve as the project moves toward production.

---

### 20.8 Pull Request Validation

Pull requests should trigger automated validation.

At minimum:

- Compile backend
- Run backend tests
- Build frontend
- Run frontend tests
- Run relevant quality checks

A pull request should not be merged if required CI checks fail.

---

### 20.9 Main Branch Protection

The primary branch should eventually have branch protection rules.

Potential requirements:

- Pull request required
- CI checks must pass
- Direct pushes restricted
- Review requirements
- No force pushes

The exact rules may be adjusted depending on whether the project is being developed individually or collaboratively.

---

### 20.10 Release Process

Production releases should be traceable to a specific source-code version.

A release may follow:

```text id="n6r1w9"
Feature / Fix
     ↓
Pull Request
     ↓
CI
     ↓
Merge
     ↓
Release
     ↓
Build Image
     ↓
Deploy
     ↓
Health Verification
```

Release versions should allow the deployed application to be identified precisely.

---

### 20.11 Database Migrations

Flyway will manage database schema changes.

Application deployment must account for database migrations explicitly.

Conceptually:

```text id="c5v8k2"
New Application Version
        ↓
Flyway Migration
        ↓
Updated Database Schema
        ↓
Application Starts
```

Migrations must be versioned and committed to source control.

---

### 20.12 Migration Safety

Database migrations should be designed carefully for production.

Destructive changes should not be introduced without considering existing application versions and existing data.

For significant schema changes, a staged approach may be required:

```text id="j2m7p4"
Add new structure
      ↓
Deploy compatible application
      ↓
Migrate / backfill data
      ↓
Switch application behavior
      ↓
Remove obsolete structure later
```

This reduces deployment risk.

---

### 20.13 Deployment Strategy

The initial deployment strategy should favor simplicity.

A deployment should:

1. Build a known application version.
2. Run automated tests.
3. Build the production image.
4. Apply required database migrations.
5. Deploy the application.
6. Verify health checks.
7. Verify basic application functionality.
8. Monitor the deployment.

More advanced strategies such as blue-green or canary deployments may be introduced later if justified.

---

### 20.14 Rollback

Every production deployment should have a rollback strategy.

Application rollback may involve redeploying a previously known-good application version.

Database rollback requires additional care.

Database migrations should not assume that every schema change can simply be reversed.

Where necessary, forward-compatible migrations should be preferred over risky destructive rollback operations.

---

### 20.15 AWS Infrastructure

AWS is the planned production cloud platform.

The exact AWS services will be selected based on the application's actual requirements.

Potential services include:

- Compute for the Spring Boot backend
- Managed PostgreSQL
- Managed Redis
- Object storage
- Container registry
- Load balancing
- DNS
- Monitoring
- Secret management

The project should avoid selecting services purely for complexity or resume value.

Infrastructure decisions should be driven by operational requirements.

---

### 20.16 Backend Deployment

The Spring Boot backend will eventually run as a containerized application.

The production architecture should provide:

- HTTPS
- Health checks
- Application logging
- Metrics
- Secure environment configuration
- Database connectivity
- Appropriate scaling capability
- Restricted network access

The backend should not expose internal infrastructure services directly to the public internet.

---

### 20.17 Frontend Deployment

The Angular frontend should be built into production assets.

The production build should:

- Disable development behavior
- Use production environment configuration
- Exclude secrets
- Use the production API URL
- Apply appropriate caching
- Use HTTPS

Frontend deployment may use a CDN/static hosting architecture.

---

### 20.18 Database Infrastructure

Production PostgreSQL should use a managed or appropriately hardened database service.

The production database should provide:

- Automated backups
- Encryption
- Restricted network access
- Monitoring
- Connection management
- Recovery procedures
- Appropriate scaling capability

The application must not expose PostgreSQL directly to public clients.

---

### 20.19 Redis Infrastructure

Redis will be introduced when caching, rate limiting, or other use cases justify it.

Production Redis should:

- Require authentication
- Use encrypted connections where supported
- Be network-restricted
- Be monitored
- Have defined failure behavior

Redis should not be treated as the authoritative source of persistent business data.

---

### 20.20 Kafka Infrastructure

Kafka will be introduced when event-driven processing provides meaningful value.

Production Kafka infrastructure should provide:

- Authentication
- Authorization
- Encryption where appropriate
- Monitoring
- Consumer monitoring
- Retry strategy
- Dead-letter handling where appropriate

Kafka should not become a dependency for simple synchronous CRUD operations without a clear architectural reason.

---

### 20.21 Object Storage

AWS S3 is the planned object-storage solution.

Storage should be used for:

- Technician photos
- Customer documents
- Equipment documents
- Invoices
- Certificates
- Reports
- Other uploaded files

Files should be stored privately by default.

The application controls authorization before generating access URLs.

---

### 20.22 Secrets Management

Production secrets must be managed outside source control.

Potential secret-management infrastructure includes:

- AWS Secrets Manager
- AWS Systems Manager Parameter Store
- Deployment-platform secret configuration

Secrets may include:

- Database credentials
- JWT signing keys
- AWS credentials
- Payment-provider credentials
- Email-provider credentials
- Kafka credentials
- Redis credentials

Secrets must not be embedded in Docker images or GitHub repositories.

---

### 20.23 IAM and Least Privilege

AWS access should follow the principle of least privilege.

Different components should receive only the permissions they require.

For example:

```text id="v8q3m6"
Backend
  ↓
S3 access required for application files

Backend
  ↓
No unnecessary administrative AWS permissions
```

Long-lived administrator credentials should not be embedded in applications or CI pipelines.

---

### 20.24 Network Architecture

Production infrastructure should separate public and private resources where appropriate.

Conceptually:

```text id="r5k1x7"
Internet
   ↓
Load Balancer / Edge
   ↓
Backend Application
   ↓
Private Network
   ├── PostgreSQL
   ├── Redis
   └── Kafka
```

Databases and internal infrastructure should not be directly exposed to the public internet.

---

### 20.25 TLS and Certificates

Production communication should use HTTPS/TLS.

This includes:

- Browser → frontend
- Browser → API
- API → external services
- API → managed infrastructure where applicable

Certificates should be managed and renewed automatically where practical.

Certificate expiration should be monitored.

---

### 20.26 Deployment Configuration

Environment-specific configuration should be externalized.

Examples include:

- Database URL
- Database credentials
- JWT configuration
- AWS configuration
- Redis configuration
- Kafka configuration
- Email provider
- Payment provider
- Frontend API URL

Application code should not contain environment-specific secrets.

---

### 20.27 CI/CD Security

The CI/CD pipeline itself is part of the security boundary.

The pipeline must protect:

- Repository secrets
- Cloud credentials
- Signing credentials
- Deployment permissions
- Production environments

GitHub Actions should use the smallest practical permissions.

Production deployments should require appropriate authorization.

---

### 20.28 Deployment Observability

Every production deployment should be observable.

The deployment process should record:

- Version deployed
- Commit SHA
- Deployment timestamp
- Environment
- Migration version
- Deployment result
- Health-check result

This information helps correlate incidents with releases.

---

### 20.29 Health Verification

After deployment, the system should verify:

- Application starts successfully
- Readiness check succeeds
- Database connectivity works
- Required dependencies are available
- Basic API functionality works
- No immediate critical errors appear

A deployment should not automatically be considered successful merely because the container started.

---

### 20.30 Scaling

The initial architecture should support horizontal scaling where practical.

Multiple backend instances should be able to run simultaneously without relying on local process memory for persistent application state.

This means:

- Persistent state belongs in the database or appropriate infrastructure.
- Authentication/session architecture must support multiple instances.
- Background jobs must avoid duplicate execution.
- Cache must be shared when required.
- File storage must not depend on local container storage.

---

### 20.31 Background Job Deployment

Background jobs must be designed for production execution.

The architecture should prevent multiple instances from unintentionally executing the same scheduled business operation simultaneously.

Possible mechanisms include:

- Distributed locks
- Database coordination
- Queue-based processing
- Idempotency
- Dedicated workers

The appropriate mechanism will depend on the specific job.

---

### 20.32 Disaster Recovery

Production infrastructure should eventually define:

- Backup strategy
- Recovery Point Objective (RPO)
- Recovery Time Objective (RTO)
- Database restoration
- Object-storage recovery
- Configuration recovery
- Infrastructure recreation
- Incident procedures

The recovery process should be tested rather than documented only theoretically.

---

### 20.33 Infrastructure as Code

Infrastructure should eventually be represented as code where practical.

Infrastructure as Code allows infrastructure to be:

- Reproduced
- Reviewed
- Versioned
- Audited
- Modified consistently

Terraform or another suitable infrastructure tool may be introduced when AWS infrastructure becomes substantial enough to justify it.

---

### 20.34 Local Development Infrastructure

Local development should be easy to reproduce.

Docker Compose may eventually provide local infrastructure such as:

```text id="f4m9c2"
Docker Compose
    ├── PostgreSQL
    ├── Redis
    └── Kafka
```

The backend and frontend may run directly from the developer's environment during development or also run inside containers depending on workflow.

---

### 20.35 Production Cost Awareness

Infrastructure decisions must consider cost.

The project should avoid unnecessarily expensive infrastructure during early stages.

Resources should scale according to actual usage.

Managed services may be preferred when they significantly reduce operational complexity, but cost and operational requirements must both be considered.

---

### 20.36 Deployment Documentation

The project should maintain documentation covering:

- Local environment setup
- Environment variables
- Docker usage
- CI/CD
- Database migrations
- Staging deployment
- Production deployment
- Rollback
- Backup and restore
- Secret management
- AWS infrastructure
- Incident procedures

A new developer should eventually be able to understand how the system moves from source code to a running environment.

---

### 20.37 Business Rules

Initial deployment and infrastructure rules:

1. Development, staging, and production environments must be isolated.
2. Production credentials must never be used for development or testing.
3. Production applications must use HTTPS.
4. Application components should be containerized where appropriate.
5. Production images must be traceable to a source-code version.
6. CI must run required automated tests before deployment.
7. Pull requests should pass required checks before merging.
8. Database schema changes must be managed through Flyway.
9. Production migrations must be designed with existing data and application compatibility in mind.
10. Production deployments must have a rollback strategy.
11. PostgreSQL must not be directly exposed to public clients.
12. Redis and Kafka must be network-restricted.
13. Object storage must keep sensitive files private by default.
14. Production secrets must remain outside source control and container images.
15. AWS permissions must follow least privilege.
16. CI/CD credentials must have only the permissions required for their operations.
17. Production infrastructure should separate public-facing and private resources where appropriate.
18. Deployment health must be verified after release.
19. Application instances must support horizontal scaling where required.
20. Persistent business state must not depend on local container storage.
21. Background jobs must prevent unintended duplicate execution.
22. Production backups and recovery procedures must be tested.
23. Infrastructure should increasingly be represented as code as the environment grows.
24. Infrastructure choices must consider operational complexity and cost.
25. Deployment versions, migrations, and health results must be traceable.
26. Production deployments should be observable through logs, metrics, and deployment records.
27. The deployment architecture should evolve according to actual system requirements rather than premature complexity.

## 21. Non-Functional Requirements & Performance

WorkFlow SaaS must satisfy non-functional requirements covering performance, scalability, availability, reliability, security, maintainability, usability, and operational capacity.

These requirements describe how the system should behave rather than what individual business features it provides.

Initial targets are intended to guide architecture and implementation.

They should be validated through measurements and load testing as the system matures.

---

### 21.1 Performance Goals

The system should provide responsive behavior for normal business operations.

The initial performance targets are:

- Typical read API requests should generally complete within approximately 300 ms under normal load.
- Typical write API requests should generally complete within approximately 500 ms under normal load.
- More expensive operations such as reports, exports, large searches, and file processing may take longer.
- Long-running operations should not unnecessarily block HTTP requests.
- Slow operations should be moved to asynchronous processing where appropriate.

Performance targets are guidelines rather than guarantees until realistic load testing has been performed.

---

### 21.2 API Response Time

API performance should be measured using latency percentiles.

The system should monitor at least:

- p50
- p95
- p99

For normal API operations, an initial target may be:

```text id="k7m2x4"
p50  → < 200 ms
p95  → < 500 ms
p99  → < 1 second
```

These values should be validated against realistic workloads.

Endpoints involving complex reports, exports, large datasets, or external services may have different targets.

---

### 21.3 Frontend Performance

The Angular frontend should provide a responsive user experience.

Performance considerations include:

- Initial application loading
- Route navigation
- API request latency
- Table rendering
- Large datasets
- Dashboard loading
- File uploads
- Search
- Mobile technician workflows

The frontend should avoid unnecessarily loading large datasets when pagination or incremental loading is sufficient.

---

### 21.4 Pagination

Large collections must not be returned without appropriate limits.

Potentially large resources include:

- Customers
- Equipment
- Service Requests
- Work Orders
- Technicians
- Inventory
- Invoices
- Notifications
- Audit events

APIs should support server-side pagination.

The backend should enforce reasonable maximum page sizes.

Clients must not be able to request unbounded amounts of data through a single normal API request.

---

### 21.5 Search Performance

Search operations should remain responsive as tenant data grows.

Search should support:

- Server-side filtering
- Pagination
- Sorting
- Appropriate indexes
- Tenant-scoped queries

Search queries must never bypass authorization or tenant filtering for performance reasons.

If more advanced search requirements emerge, a dedicated search technology may be evaluated later.

---

### 21.6 Dashboard Performance

Dashboards may combine information from multiple domains.

Dashboard queries should be designed carefully to avoid repeatedly executing expensive queries.

Potential optimization techniques include:

- Proper indexes
- Aggregated queries
- Projections
- Caching
- Precomputed metrics
- Asynchronous report generation

Dashboard performance must not come at the cost of tenant isolation or authorization.

---

### 21.7 Reporting Performance

Reports can require significantly more processing than normal CRUD operations.

Small reports may be generated synchronously.

Large reports should eventually use asynchronous processing:

```text id="p4n8c1"
User requests report
       ↓
Report Job Created
       ↓
Background Processing
       ↓
Report Generated
       ↓
User Notified
       ↓
Download
```

Report generation must remain tenant-scoped and permission-controlled.

---

### 21.8 File Upload Performance

File operations should not unnecessarily block application threads.

Large files may eventually use direct-to-object-storage uploads where appropriate.

The backend must still authorize the upload and control the resulting storage location.

File size limits should be configurable.

The system should provide useful progress and failure feedback to the frontend.

---

### 21.9 Database Performance

PostgreSQL is the primary persistence layer and must remain performant under expected workloads.

Performance considerations include:

- Indexes
- Query plans
- Connection pooling
- Pagination
- Efficient joins
- Avoiding N+1 queries
- Appropriate fetch strategies
- Batch operations
- Transaction duration
- Lock contention

Database optimization should be based on measured query behavior rather than premature optimization.

---

### 21.10 Connection Pooling

The backend should use a controlled database connection pool.

The pool must be configured according to:

- Application instance count
- Database capacity
- Expected concurrency
- Query duration
- Deployment environment

Too many connections can overload PostgreSQL.

When horizontal scaling is introduced, the total connection usage across all instances must be considered.

---

### 21.11 Redis Performance

Redis may be used for:

- Caching
- Rate limiting
- Distributed coordination
- Temporary state
- Other performance-sensitive operations

Redis should not become a mandatory dependency for basic persistent business operations unless explicitly required.

If Redis becomes unavailable, the system should have clearly defined fallback behavior.

---

### 21.12 Kafka Performance

Kafka will support asynchronous event processing where justified.

Performance considerations include:

- Event throughput
- Consumer throughput
- Consumer lag
- Batch processing
- Retry behavior
- Partitioning
- Ordering requirements

Kafka should be introduced based on actual asynchronous processing requirements rather than simply because it is available.

---

### 21.13 Background Processing

Long-running or resource-intensive operations should be processed asynchronously.

Potential candidates include:

- Report generation
- Large exports
- Email delivery
- Notification processing
- File processing
- Maintenance-plan evaluation
- Subscription processing
- Large data imports

Asynchronous processing must provide status visibility and appropriate failure handling.

---

### 21.14 Concurrency

The application must support multiple users operating simultaneously.

Potential concurrent operations include:

- Multiple managers assigning Work Orders
- Multiple technicians updating jobs
- Multiple users modifying customer information
- Multiple users consuming inventory
- Multiple users creating invoices
- Background jobs operating while users interact with the system

Concurrency controls must prevent invalid states.

---

### 21.15 Optimistic Locking

Optimistic locking should be used where concurrent modification is a realistic risk.

For example:

```text id="v9c3m6"
Manager A loads Work Order
Manager B loads same Work Order

Manager A updates
       ↓
Version = 2

Manager B attempts update
       ↓
Version mismatch
       ↓
Conflict
```

The API should communicate the conflict appropriately rather than silently overwriting another user's changes.

---

### 21.16 Scalability

The initial architecture should support scaling without requiring a complete redesign.

The modular monolith should be capable of scaling horizontally where practical.

Potential scaling dimensions include:

- API instances
- Background workers
- Database capacity
- Redis capacity
- Kafka consumers
- Object storage
- CDN/static frontend delivery

Scaling decisions should be based on measured bottlenecks.

---

### 21.17 Stateless Application Instances

Backend application instances should remain stateless wherever practical.

Persistent state should live in appropriate external systems such as:

- PostgreSQL
- Redis
- Object storage
- Kafka

This allows multiple backend instances to process requests without relying on local process memory.

---

### 21.18 Availability

The initial production target should prioritize reliable operation without requiring excessive infrastructure complexity.

An initial availability target may be approximately:

**99.5% monthly availability**

This target should be revisited once real production requirements and usage patterns are known.

Availability measurements should exclude explicitly defined planned maintenance windows where appropriate.

---

### 21.19 Reliability

The application should behave predictably when dependencies fail.

Examples:

```text id="x2m7p5"
Email provider unavailable
        ↓
Queue / retry
        ↓
Core Work Order operation remains successful
```

and:

```text id="h6r1w8"
Redis unavailable
        ↓
Defined fallback behavior
        ↓
Application remains operational where possible
```

A non-critical dependency failure should not unnecessarily bring down unrelated core functionality.

---

### 21.20 Fault Isolation

Failures should remain contained where possible.

For example:

- Email failure should not prevent Work Order creation.
- Report generation failure should not prevent normal API operations.
- One failed Kafka event should not stop unrelated event processing.
- One tenant's expensive report should not exhaust resources for every tenant.

Resource-intensive operations should have appropriate limits and isolation mechanisms.

---

### 21.21 Resource Limits

The system should enforce reasonable limits for resource-intensive operations.

Potential limits include:

- Maximum request body size
- Maximum file size
- Maximum page size
- Maximum export size
- Maximum report duration
- Maximum concurrent jobs
- Maximum API request rate
- Maximum tenant storage
- Maximum subscription usage

Some limits may be determined by subscription plan.

---

### 21.22 Subscription Capacity Limits

Subscription plans may eventually control resource limits.

Examples include:

- Maximum users
- Maximum technicians
- Maximum customers
- Maximum equipment
- Maximum storage
- Maximum monthly Work Orders
- Maximum API usage
- Maximum report/export capacity

The backend must enforce these limits.

Frontend restrictions alone are insufficient.

---

### 21.23 Multi-Tenant Resource Fairness

One tenant must not be able to consume an unreasonable amount of shared resources and negatively affect other tenants.

Potential controls include:

- Rate limiting
- Usage limits
- File quotas
- Job concurrency limits
- Report limits
- API pagination limits
- Background queue controls

Tenant resource isolation should evolve as actual usage patterns become known.

---

### 21.24 Availability of Critical Operations

Critical business operations should remain available whenever their required dependencies are healthy.

Critical operations include:

- Authentication
- Service Request creation
- Work Order creation
- Work Order assignment
- Work Order status updates
- Technician workflows
- Customer access

Non-critical features such as advanced reporting may be temporarily degraded without making the core system unavailable.

---

### 21.25 Graceful Degradation

Where possible, the application should degrade gracefully when optional infrastructure fails.

Examples:

```text id="n3k8q6"
Advanced reporting unavailable
        ↓
Core Work Orders remain available
```

or:

```text id="t7p2m9"
Email delivery unavailable
        ↓
Notification stored
        ↓
Delivery retried later
```

The system should distinguish between unavailable functionality and complete application failure.

---

### 21.26 Timeout Management

External operations must have appropriate timeouts.

The application should not allow an unavailable external service to block threads indefinitely.

Timeouts should be considered for:

- HTTP calls
- Database operations
- Redis
- Kafka operations
- File storage
- Email providers
- Payment providers

Timeout values should be based on realistic operation requirements.

---

### 21.27 Retry Strategy

Retries should be used selectively.

Retries are appropriate when:

- Failure is likely temporary.
- The operation is safe to retry.
- The dependency supports repeated requests safely.

Retries should not be used blindly for every error.

Retry behavior should include:

- Maximum attempts
- Delay/backoff
- Failure classification
- Idempotency protection

---

### 21.28 Capacity Planning

Capacity planning should consider:

- Number of tenants
- Users per tenant
- Requests per second
- Work Orders per day
- File storage volume
- Database size
- Notification volume
- Kafka event volume
- Report generation volume

Initial capacity estimates may be approximate.

Actual measurements should replace assumptions as the platform grows.

---

### 21.29 Load Testing

Load testing should eventually simulate realistic workloads.

Scenarios may include:

- Concurrent logins
- Service Request creation
- Work Order updates
- Dashboard access
- Search
- Scheduling
- File uploads
- Report generation

Load testing should measure:

- Throughput
- Latency
- Error rate
- CPU
- Memory
- Database utilization
- Connection pool usage
- Infrastructure saturation

---

### 21.30 Stress Testing

Stress testing should intentionally exceed expected normal capacity to determine how the system behaves under pressure.

The objective is to identify:

- Failure points
- Resource exhaustion
- Slowdowns
- Cascading failures
- Recovery behavior

The system should fail predictably rather than silently corrupting data.

---

### 21.31 Recovery Performance

Performance requirements also apply to recovery.

The architecture should eventually define:

- Recovery Point Objective (RPO)
- Recovery Time Objective (RTO)

These values will depend on the final production business requirements.

For example:

```text id="w5m1c7"
RPO
How much data loss is acceptable?

RTO
How long can the system remain unavailable?
```

The final values should be established before production reaches business-critical usage.

---

### 21.32 Maintainability

The system should remain understandable and maintainable as it grows.

Maintainability requirements include:

- Clear module boundaries
- Consistent naming
- Small focused components
- Documented business rules
- Automated tests
- API documentation
- Database migrations
- Operational documentation
- Meaningful logs
- Avoidance of unnecessary technical complexity

The modular monolith architecture is intended to support maintainability while keeping deployment simple.

---

### 21.33 Extensibility

The architecture should allow future capabilities without major rewrites.

Potential future extensions include:

- Mobile applications
- Additional notification channels
- Advanced scheduling
- AI-assisted diagnostics
- More payment providers
- Additional integrations
- Customer APIs
- Advanced analytics
- Microservice extraction where justified

Future extensibility must not justify unnecessary abstraction in the initial implementation.

---

### 21.34 Compatibility

The system should maintain compatibility between:

- Frontend and backend
- API versions
- Database schema and application versions
- Event producers and consumers
- File metadata and stored files

Breaking changes should be deliberate and documented.

---

### 21.35 Accessibility

The Angular frontend should support accessible interaction.

Requirements include:

- Keyboard navigation
- Appropriate labels
- Semantic controls
- Visible focus states
- Meaningful error messages
- Sufficient contrast
- Accessible forms
- Screen-reader compatibility where practical

Accessibility should be considered during component design rather than added only at the end.

---

### 21.36 Mobile Usability

Technician workflows should work effectively on mobile devices.

Important mobile workflows include:

- Viewing assigned Work Orders
- Starting work
- Pausing work
- Completing work
- Adding notes
- Uploading photos
- Recording measurements
- Recording parts
- Customer confirmation

The interface should avoid unnecessary complexity during field work.

---

### 21.37 Data Integrity

Performance must never take priority over data correctness for critical business operations.

Examples include:

- Inventory
- Financial calculations
- Work Order state transitions
- Assignments
- Scheduling
- Tenant boundaries
- Audit history

Optimization must not introduce race conditions or inconsistent data.

---

### 21.38 Security vs Performance

Security controls must not be removed simply to improve performance.

Examples:

- Tenant filtering must remain enforced.
- Authorization must remain enforced.
- File access checks must remain enforced.
- Input validation must remain enforced.

If security checks become a performance bottleneck, the implementation should be optimized rather than bypassed.

---

### 21.39 Performance Measurement

Performance requirements must eventually be validated through measurements.

Potential tools and techniques include:

- Application metrics
- Database query analysis
- Profiling
- Load testing
- JVM monitoring
- Prometheus
- Grafana
- Application Performance Monitoring where appropriate

Performance assumptions should be replaced with measured results as the system matures.

---

### 21.40 Business Rules

Initial non-functional requirements:

1. Normal API operations should target responsive latency under expected load.
2. API performance must be measured using latency percentiles.
3. Large collections must use server-side pagination.
4. Backend APIs must enforce maximum page sizes.
5. Search must remain tenant-scoped and authorization-aware.
6. Dashboard queries must be designed for predictable performance.
7. Large reports and exports should use asynchronous processing where appropriate.
8. Database performance must be monitored and optimized based on measurements.
9. Database connection pools must be configured according to actual capacity.
10. Redis must not become the authoritative source of persistent business data.
11. Kafka must be introduced only where asynchronous processing provides meaningful value.
12. The application must support concurrent users without corrupting business state.
13. Optimistic locking should protect important concurrently modified entities.
14. The modular monolith should support horizontal scaling where practical.
15. Backend instances should remain stateless where possible.
16. Initial production availability should target approximately 99.5% monthly availability.
17. Non-critical dependency failures should not unnecessarily disable core business functionality.
18. Resource-intensive operations must have appropriate limits.
19. Subscription plans may enforce tenant resource limits.
20. One tenant must not be able to consume unreasonable shared resources.
21. Critical business operations should remain available when their required dependencies are healthy.
22. Optional functionality should degrade gracefully where practical.
23. External operations must use appropriate timeouts.
24. Retries must be bounded, intentional, and safe.
25. Load testing should be performed before relying on production capacity assumptions.
26. Stress testing should verify controlled failure behavior.
27. RPO and RTO should be defined before production becomes business-critical.
28. Maintainability is a non-functional requirement.
29. The architecture should remain extensible without introducing unnecessary complexity.
30. Accessibility and mobile usability are product requirements, not optional enhancements.
31. Performance optimization must never bypass security or data-integrity controls.
32. Performance requirements should ultimately be validated through measurement rather than assumptions.

## 22. Data Privacy, Compliance & Data Governance

WorkFlow SaaS will process business and potentially personal information belonging to tenants, customers, employees, technicians, requesters, and other users.

Data privacy and governance must therefore be considered throughout the system lifecycle.

The platform should minimize unnecessary data collection, restrict access to authorized users, protect stored and transmitted data, maintain appropriate retention rules, and provide controlled mechanisms for data export and deletion.

Specific legal or regulatory obligations must be validated against the final markets, customers, data types, and deployment model before production use.

---

### 22.1 Data Governance Goals

The data governance model should provide:

- Clear ownership of tenant data
- Defined data categories
- Controlled access
- Data minimization
- Appropriate retention
- Secure deletion
- Data export capabilities
- Auditability
- Protection against unauthorized disclosure
- Clear handling of backups
- Controlled administrative access

---

### 22.2 Data Ownership

Tenant data belongs conceptually to the tenant organization that creates or manages it.

WorkFlow SaaS provides the platform used to process that data.

The system should distinguish between:

- Platform-owned operational data
- Tenant business data
- User account data
- Customer data
- System-generated audit data
- Application telemetry
- Temporary processing data

The exact contractual ownership and processing terms must be defined separately from the technical architecture.

---

### 22.3 Data Categories

The system may process several categories of information.

#### Identity Data

Examples:

- Name
- Email
- Phone number
- User identifier
- Account status

#### Organization Data

Examples:

- Tenant name
- Company details
- Subscription information
- Business configuration

#### Customer Data

Examples:

- Customer name
- Contact information
- Locations
- Customer users
- Service history

#### Operational Data

Examples:

- Service Requests
- Work Orders
- Technician assignments
- Scheduling information
- Equipment records
- Maintenance history
- Inventory movements

#### Financial Data

Examples:

- Invoices
- Invoice lines
- Prices
- Taxes
- Payment status
- Subscription information

#### Document Data

Examples:

- Technician photographs
- Certificates
- Manuals
- Reports
- Invoices
- Customer attachments

#### Audit and Security Data

Examples:

- Login events
- Administrative actions
- Resource changes
- Correlation identifiers
- Security events
- IP information where appropriate

---

### 22.4 Data Minimization

The system should collect only data necessary for the functionality being provided.

For example, a user profile should not collect personal information that has no defined business purpose.

Optional fields should remain optional unless a business requirement requires them.

Data minimization should also apply to:

- Logs
- JWT claims
- Events
- Notifications
- Analytics
- Audit records
- API responses

---

### 22.5 Purpose Limitation

Data should be used for defined business or technical purposes.

For example:

```text id="j6m3q8"
Technician phone number
        ↓
Operational communication
```

does not automatically mean the same data should be:

```text id="x8p2v5"
Technician phone number
        ↓
Unrelated analytics
```

Use of data for additional purposes should be evaluated separately.

---

### 22.6 Data Access

Access to data must follow the authorization model defined in the security architecture.

Access should consider:

```text id="m4r7k2"
User
 ↓
Tenant Membership
 ↓
Role / Permission
 ↓
Customer Scope where applicable
 ↓
Resource
```

Users should receive only the information necessary for their authorized operations.

---

### 22.7 Administrative Access

Platform administrators may require access to certain operational information.

Administrative access should be:

- Explicitly authorized
- Limited to necessary operations
- Audited
- Protected by strong authentication
- Reviewed where appropriate

Platform administrators should not automatically have unrestricted access to all tenant business data merely because they have platform-level privileges.

Where support access to tenant data is required, the access model should be explicitly defined and auditable.

---

### 22.8 Sensitive Data

The system should identify information requiring additional protection.

Potentially sensitive information may include:

- Authentication credentials
- Security tokens
- Personal contact information
- Financial information
- Private customer documents
- Technician photographs
- Internal notes
- Security audit information

Sensitive information should receive appropriate access controls and storage protections.

---

### 22.9 Password and Authentication Data

Authentication secrets receive special treatment.

The system must never store:

- Plaintext passwords
- Recoverable passwords
- Passwords inside JWT tokens

Password-reset credentials and other authentication tokens should be short-lived and protected.

---

### 22.10 Encryption in Transit

Sensitive data transmitted between systems should use encrypted connections.

Production communication should use HTTPS/TLS.

This applies to:

- Browser → frontend
- Browser → API
- API → database where supported
- API → Redis where supported
- API → Kafka where supported
- API → object storage
- API → external services

---

### 22.11 Encryption at Rest

Sensitive production data should use encryption at rest where supported.

Potential protected resources include:

- PostgreSQL
- Object storage
- Backups
- Redis where appropriate
- Kafka storage where applicable

Encryption keys and credentials must be managed separately from application source code.

---

### 22.12 Data Retention

Different categories of data may require different retention periods.

Potential categories include:

- User accounts
- Service Requests
- Work Orders
- Equipment history
- Invoices
- Audit events
- Notifications
- Uploaded files
- Application logs
- Security logs
- Backups

Retention periods should be defined according to business, legal, contractual, and operational requirements.

The system should avoid retaining data indefinitely without a defined reason.

---

### 22.13 Deletion and Archival

Deletion must consider historical integrity.

Some records should not be physically deleted immediately because they may be required for:

- Audit
- Financial history
- Operational history
- Legal obligations
- Reporting
- Referential integrity

Where appropriate, records may instead be:

- Archived
- Soft-deleted
- Anonymized
- Retained for a defined period

The correct behavior must be defined per entity.

---

### 22.14 User Account Deletion

Deleting a user account must not automatically destroy historical business records that require preservation.

For example:

```text id="p8w2n6"
Technician completes Work Order
        ↓
Technician account later removed
        ↓
Historical Work Order remains
```

Historical records may retain a controlled reference to the former user or use an appropriate anonymization strategy depending on requirements.

---

### 22.15 Tenant Data Deletion

Tenant deletion is a high-impact operation.

The system should define a controlled tenant offboarding process.

Potential lifecycle:

```text id="v3k7m1"
Tenant requests closure
        ↓
Account suspended / closure initiated
        ↓
Retention period
        ↓
Data export if required
        ↓
Deletion / archival
        ↓
Storage cleanup
```

Tenant deletion must account for:

- Database records
- Files
- Notifications
- Audit records
- Cache
- Events
- Search indexes
- Backups

Deleted tenant data must not remain accidentally accessible through another subsystem.

---

### 22.16 Tenant Data Export

The platform should eventually support controlled tenant data export.

Possible exported information includes:

- Customers
- Locations
- Equipment
- Service Requests
- Work Orders
- Technicians
- Maintenance
- Inventory
- Invoices
- Documents
- Relevant configuration

Exports must:

- Be authorized
- Be tenant-scoped
- Be auditable
- Avoid exposing other tenants
- Use secure temporary storage
- Have controlled expiration

Large exports should use asynchronous processing.

---

### 22.17 Data Portability

The architecture should make it possible to extract tenant data in structured formats.

Potential formats include:

- JSON
- CSV
- Excel-compatible formats
- PDF for human-readable reports
- Original uploaded files

The exact export format will depend on the type of data.

---

### 22.18 Backup Data

Backups are copies of production data and therefore require equivalent protection.

Backup systems must consider:

- Encryption
- Access control
- Retention
- Geographic/storage redundancy where appropriate
- Deletion policies
- Restore testing

Deleting live data does not necessarily mean it disappears immediately from backups.

Backup retention and eventual expiration must therefore be defined explicitly.

---

### 22.19 Logs and Privacy

Logs must follow data-minimization principles.

The application should avoid logging unnecessary:

- Personal information
- Request bodies
- Customer documents
- Authentication information
- Financial details

Where an identifier is sufficient for troubleshooting, logging the identifier may be preferable to logging the full underlying data.

---

### 22.20 Audit Records and Privacy

Audit records are important for security and accountability but may themselves contain personal information.

Audit records should therefore:

- Be access-controlled
- Be protected from unauthorized modification
- Have defined retention
- Avoid unnecessary sensitive data
- Be available only to authorized roles

Audit requirements must be balanced with data-minimization requirements.

---

### 22.21 File Privacy

Uploaded documents should be private by default.

Access must be determined by:

- Authenticated user
- Tenant
- Customer scope
- Related entity
- Permission
- File visibility rules

Public object-storage URLs should not be used for sensitive files unless explicitly intended.

---

### 22.22 Personal Data in Documents

Documents may contain information that is not visible from their metadata.

For example:

- Technician photographs
- Customer reports
- Scanned documents
- Certificates
- Invoices

The system should therefore treat uploaded files as potentially sensitive even when their filenames or metadata appear harmless.

---

### 22.23 Data Classification

The project should eventually classify data according to sensitivity.

A simple initial model may be:

```text id="c5m9r2"
PUBLIC
   ↓
INTERNAL
   ↓
CONFIDENTIAL
   ↓
SENSITIVE
```

Classification should influence:

- Access control
- Logging
- Storage
- Encryption
- Export
- Retention

The exact classification scheme may evolve.

---

### 22.24 Privacy by Design

Privacy should be considered during feature design rather than added after implementation.

Examples:

- Only request necessary user information.
- Do not place sensitive data in URLs unnecessarily.
- Do not expose unnecessary fields through DTOs.
- Do not include sensitive information in notifications unless required.
- Do not include sensitive information in events unless required.
- Do not expose private files by default.

---

### 22.25 API Data Exposure

API responses should return only fields required by the client.

JPA entities should not automatically be serialized directly into API responses.

DTOs should explicitly define exposed fields.

This reduces the risk of accidentally exposing:

- Internal identifiers
- Security information
- Internal notes
- Sensitive configuration
- Unnecessary personal information

---

### 22.26 Search and Privacy

Search functionality must respect all authorization boundaries.

A user searching for:

```text id="n7q4x1"
"Customer ABC"
```

must only receive results they are authorized to see.

Search indexes, caches, and autocomplete systems must also preserve the same access boundaries.

---

### 22.27 Notifications and Privacy

Notifications should contain only information appropriate for their recipient.

For example, an email notification should not unnecessarily expose sensitive internal information.

Notifications sent to customers should not include:

- Internal technician notes
- Internal pricing information unless intended
- Internal operational comments
- Security-sensitive information

Recipient scope must be validated before delivery.

---

### 22.28 Third-Party Data Sharing

External providers may process information on behalf of WorkFlow SaaS.

Potential providers include:

- Cloud infrastructure
- Email provider
- Payment provider
- Object storage
- Monitoring systems
- Analytics services
- Future integrations

The platform should minimize the data sent to external providers and document important data flows.

---

### 22.29 Data Processing Documentation

The project should maintain documentation describing significant data flows.

For example:

```text id="w2k8p4"
User
 ↓
Angular
 ↓
Spring Boot
 ↓
PostgreSQL

Technician Photo
 ↓
Spring Boot
 ↓
Object Storage

Invoice Payment Event
 ↓
Payment Provider
 ↓
Webhook
 ↓
Spring Boot
```

This documentation will make privacy and security reviews easier.

---

### 22.30 Data Residency

The production architecture should document where important data is physically or logically stored.

Potential data locations include:

- Primary database region
- Object-storage region
- Backup region
- Monitoring systems
- External SaaS providers

The final deployment region and data residency requirements should be evaluated against the target customer market and applicable obligations.

---

### 22.31 Privacy Requests

Depending on applicable requirements and contractual obligations, the platform may eventually need to support requests such as:

- Data access
- Data export
- Data correction
- Data deletion
- Account closure
- Data restriction

The exact workflows will depend on applicable legal requirements.

Technical capabilities should be designed so these operations can be performed in a controlled and auditable manner.

---

### 22.32 Data Correction

Users must be able to correct normal mutable business information where authorized.

However, historical audit and financial records should not be silently rewritten.

For example:

```text id="r6m3v9"
Incorrect customer information
        ↓
Authorized correction
        ↓
Current customer record updated
        ↓
Historical audit preserved
```

Where historical values must remain immutable, a corrective event or new version should be recorded instead.

---

### 22.33 Compliance Readiness

The system should be designed to support compliance requirements without claiming compliance automatically.

Compliance depends on more than application code.

It may also require:

- Policies
- Contracts
- Organizational procedures
- Access reviews
- Incident response
- Vendor management
- Employee training
- Infrastructure configuration
- Documentation
- Audits

The technical architecture should provide the necessary controls where applicable.

---

### 22.34 Security Incident Handling

Potential privacy or security incidents should be detectable and traceable.

Examples include:

- Unauthorized access
- Credential compromise
- Data leakage
- Cross-tenant access
- Malicious file upload
- Compromised integration
- Accidental disclosure

The operational architecture should support investigation using:

- Logs
- Audit events
- Correlation IDs
- Metrics
- Traces where available

Incident-response procedures will be documented separately as the production environment matures.

---

### 22.35 Data Lifecycle

Data should have a defined lifecycle where appropriate:

```text id="h8p2m5"
Created
  ↓
Active
  ↓
Updated / Used
  ↓
Archived
  ↓
Retention Period
  ↓
Deleted / Anonymized
```

Not every entity will use exactly this lifecycle.

Financial, audit, and historical records may have different retention behavior.

---

### 22.36 Business Rules

Initial data privacy and governance rules:

1. The system should collect only data required for defined business or technical purposes.
2. Tenant data must remain isolated from other tenants.
3. Customer data must remain isolated according to customer authorization boundaries.
4. Access to personal and sensitive data must follow the authorization model.
5. Administrative access must be explicitly controlled and auditable.
6. Passwords and authentication secrets must never be stored in recoverable plaintext form.
7. Sensitive production communication must use encrypted transport.
8. Sensitive production data should use encryption at rest where supported.
9. Data retention periods should be defined by data category.
10. Data should not be retained indefinitely without a defined purpose.
11. Deletion behavior must account for historical, financial, audit, and referential requirements.
12. User deletion must not unnecessarily destroy required historical business records.
13. Tenant deletion must account for database, files, cache, events, search, and backups.
14. Tenant data export must be authorized, tenant-scoped, secure, and auditable.
15. Large data exports should use asynchronous processing.
16. Backups must receive appropriate security controls and retention policies.
17. Logs must minimize unnecessary personal and sensitive information.
18. Audit records must be access-controlled and protected from unauthorized modification.
19. Uploaded files must be treated as potentially sensitive.
20. Private files must not be publicly accessible by default.
21. API responses must expose only required data.
22. JPA entities should not be serialized directly as API contracts.
23. Search results must respect authorization and tenant boundaries.
24. Notifications must contain only information appropriate for their recipients.
25. External providers should receive only the data necessary for their function.
26. Important data flows should be documented.
27. Data residency should be documented for production infrastructure and important external providers.
28. Privacy-related requests should be handled through controlled and auditable workflows where applicable.
29. Historical audit and financial records must not be silently rewritten.
30. The platform should support privacy and compliance requirements through technical controls but must not claim legal compliance without appropriate organizational and legal validation.
31. Security and privacy incidents must be traceable through appropriate operational records.
32. Data lifecycle and retention behavior should be defined for important entity categories.

## 23. API & Integration Strategy

WorkFlow SaaS will communicate with external systems through well-defined integration boundaries.

The integration architecture must support the current product while allowing future integrations without tightly coupling the core business logic to individual external providers.

Potential integrations include:

- Payment providers
- Email providers
- Cloud storage
- Webhook consumers
- Customer systems
- Accounting systems
- Calendar systems
- Mobile applications
- Third-party automation platforms
- Future public API consumers

The integration architecture should prioritize security, tenant isolation, reliability, observability, and maintainability.

---

### 23.1 Integration Principles

External integrations should follow these principles:

- Explicit contracts
- Least-privilege access
- Tenant isolation
- Secure authentication
- Input/output validation
- Timeout protection
- Controlled retries
- Idempotency
- Error handling
- Observability
- Auditability
- Provider abstraction
- Versioning
- Graceful degradation

The core domain should not depend directly on a specific third-party implementation whenever practical.

---

### 23.2 Integration Categories

Integrations can be divided into several categories.

#### Platform Integrations

Used internally by WorkFlow SaaS.

Examples:

- PostgreSQL
- Redis
- Kafka
- Object storage
- Monitoring systems

#### External Service Integrations

Third-party services used by the platform.

Examples:

- Payment provider
- Email provider
- SMS provider
- Authentication provider
- Analytics provider

#### Customer Integrations

Systems connected by WorkFlow SaaS customers.

Examples:

- Accounting software
- ERP systems
- CRM systems
- Customer portals
- Internal company systems

#### Client Applications

Applications consuming the WorkFlow API.

Examples:

- Angular web application
- Future mobile application
- Customer applications
- Partner applications

---

### 23.3 Internal vs External APIs

The system should distinguish between:

- Internal application APIs
- Public APIs
- Webhooks
- Provider-specific APIs

Internal APIs support the WorkFlow frontend and internal clients.

Public APIs are designed for external consumers and require stronger compatibility and versioning guarantees.

Provider APIs are external interfaces controlled by third parties and should be isolated behind integration adapters.

---

### 23.4 API Versioning

Public APIs should be versioned.

Initial API structure:

```text id="a7m2k9"
https://api.example.com/api/v1/...
```

A future incompatible version may use:

```text id="q4p8v3"
https://api.example.com/api/v2/...
```

Breaking API changes should not silently alter the behavior expected by existing clients.

---

### 23.5 Integration Adapters

External providers should be accessed through dedicated integration components.

For example:

```text id="k5r9m2"
Billing Module
      ↓
PaymentService
      ↓
PaymentProviderAdapter
      ↓
Stripe / Other Provider
```

The business logic should depend on the internal abstraction rather than directly on provider-specific classes wherever practical.

This makes it easier to:

- Replace providers
- Test integrations
- Mock external systems
- Handle provider-specific behavior
- Avoid spreading third-party code throughout the application

---

### 23.6 Payment Provider Integration

Future SaaS billing may integrate with a payment provider.

The integration should support:

- Customer creation
- Subscription creation
- Subscription changes
- Payment status
- Cancellation
- Payment failures
- Webhooks

Payment provider identifiers should be stored separately from internal WorkFlow identifiers.

The internal subscription remains the source of truth for WorkFlow business behavior.

---

### 23.7 Webhook Processing

External systems may notify WorkFlow through webhooks.

Example:

```text id="n3w8p5"
Payment Provider
      ↓
Webhook
      ↓
WorkFlow API
      ↓
Validation
      ↓
Authentication
      ↓
Idempotency Check
      ↓
Business Processing
      ↓
Audit / Event
```

Webhook processing must verify that the request originates from the expected provider.

Where supported, signatures should be validated.

---

### 23.8 Webhook Idempotency

Webhook delivery may occur more than once.

The system must therefore prevent duplicate business effects.

Each webhook should have a provider-specific event identifier where available.

Processed events should be tracked.

Example:

```text id="c8m4r7"
Webhook Event
     ↓
Already processed?
   ↙       ↘
 YES       NO
  ↓         ↓
Ignore    Process
            ↓
        Mark processed
```

This is particularly important for:

- Payments
- Subscription changes
- Invoice events
- External status updates

---

### 23.9 Outbound Webhooks

WorkFlow SaaS should eventually allow tenants to subscribe to events generated by their organization.

Potential events include:

- Service Request created
- Service Request approved
- Work Order created
- Work Order assigned
- Work Order scheduled
- Work Order completed
- Work Order cancelled
- Maintenance due
- Invoice issued
- Invoice paid

Example:

```text id="t6p2v9"
WorkFlow Event
      ↓
Webhook Delivery
      ↓
Customer System
```

Outbound webhooks must be tenant-specific.

A tenant must never receive events belonging to another tenant.

---

### 23.10 Webhook Security

Outbound webhook requests should be protected against unauthorized consumption.

Potential mechanisms include:

- Signing secrets
- Request signatures
- Timestamp validation
- Event identifiers
- Replay protection
- HTTPS
- Delivery authentication

Webhook secrets must never be exposed through normal API responses or logs.

---

### 23.11 Webhook Retry Strategy

Temporary delivery failures should be retried.

Retry behavior should use bounded attempts and increasing delays.

Example:

```text id="v8k3m5"
Attempt 1
   ↓
Failure
   ↓
Wait
   ↓
Attempt 2
   ↓
Failure
   ↓
Wait longer
   ↓
Attempt 3
```

The system should avoid unlimited retries.

After the retry limit is reached, the delivery should enter a failed state and become observable to administrators.

---

### 23.12 Email Integration

Email delivery should be isolated behind an internal notification abstraction.

Example:

```text id="m4q7x2"
Notification Service
       ↓
Email Service
       ↓
Email Provider Adapter
       ↓
External Provider
```

This prevents business modules from directly depending on a specific email provider.

Potential email messages include:

- User invitations
- Password reset
- Work Order notifications
- Maintenance reminders
- Invoice notifications
- Subscription notifications
- Security alerts

---

### 23.13 Email Delivery Reliability

Email delivery should support:

- Delivery status
- Failure tracking
- Retry where appropriate
- Provider response tracking
- Correlation IDs
- Tenant context
- Idempotency where necessary

The system should avoid sending duplicate emails when the same business event is processed more than once.

---

### 23.14 Object Storage Integration

Files will eventually be stored in object storage such as AWS S3.

The application should store file metadata in PostgreSQL while the actual file content remains in object storage.

Example:

```text id="r2n6p8"
Application
    ↓
File Metadata → PostgreSQL
    ↓
File Content → Object Storage
```

Storage access should be mediated through a file-storage abstraction.

---

### 23.15 Storage Isolation

Object storage must preserve tenant boundaries.

Storage keys should include sufficient tenant context to prevent accidental collisions.

Conceptually:

```text id="x7m3q9"
tenant/{tenantId}/files/{fileId}
```

The exact storage-key structure may change during implementation.

Storage paths must never be treated as a substitute for authorization.

The application must still verify that the requesting user is authorized to access the file.

---

### 23.16 Calendar Integrations

A future integration may allow WorkFlow schedules to synchronize with external calendar systems.

Potential functionality:

- Create calendar events
- Update events
- Cancel events
- Synchronize schedule changes
- Handle timezone differences

Calendar integration should not become the source of truth for WorkFlow scheduling.

WorkFlow scheduling remains authoritative.

---

### 23.17 Accounting Integrations

Future integrations may synchronize financial information with accounting systems.

Potential information includes:

- Customers
- Invoices
- Invoice lines
- Taxes
- Payment status

Accounting integrations must preserve the distinction between:

```text id="f6q2m8"
WorkFlow internal financial records
            ↓
External accounting representation
```

External synchronization must not silently modify internal historical financial records.

---

### 23.18 Customer API Access

Tenants may eventually receive API credentials allowing their own systems to communicate with WorkFlow.

API credentials should be:

- Tenant-specific
- Revocable
- Scoped
- Expirable where appropriate
- Auditable
- Securely stored

Credentials should never provide unrestricted platform access.

---

### 23.19 API Scopes

External API access should use scopes or permissions where appropriate.

Example:

```text id="p9r4k6"
work_orders:read
work_orders:write
customers:read
customers:write
invoices:read
reports:read
```

A credential should receive only the scopes required for its integration.

---

### 23.20 API Rate Limiting

External API consumers should be subject to rate limits.

Limits may eventually vary according to:

- Tenant
- API credential
- Endpoint
- Subscription plan
- Request type

Rate limiting protects the platform from:

- Accidental overload
- Runaway integrations
- Abuse
- Resource exhaustion

Rate-limit responses should communicate when clients can retry where appropriate.

---

### 23.21 Integration Timeouts

External calls must have explicit timeouts.

The application should never allow an unavailable external provider to block resources indefinitely.

For example:

```text id="w5n8q3"
WorkFlow
   ↓
External Provider
   ↓
Timeout
   ↓
Controlled Failure
```

Timeout values should be configured according to the operation.

---

### 23.22 Retry Policy

Retries should only be performed when the operation is safe to retry.

Transient failures may be retried.

Permanent failures should generally not be retried repeatedly.

Examples of potentially retryable conditions:

- Temporary network failure
- Connection timeout
- Temporary provider unavailability
- Rate limiting where retry is explicitly supported

Examples of conditions that may require immediate handling:

- Invalid credentials
- Invalid request
- Authorization failure
- Permanent validation error

Retries must use bounded attempts and appropriate backoff.

---

### 23.23 Idempotent Operations

Operations that may be retried should support idempotency where appropriate.

Potential examples:

- Payment creation
- Invoice submission
- Webhook processing
- External synchronization
- Outbound webhook delivery

Idempotency keys or equivalent identifiers should prevent duplicate business effects.

---

### 23.24 Integration Failures

External integration failures must not expose internal implementation details to API clients.

The system should distinguish between:

```text id="c4v7m2"
Business Validation Error
        ↓
Client must correct request
```

and:

```text id="k8p3n5"
External Provider Failure
        ↓
WorkFlow may retry / queue / report failure
```

The API should return a controlled error response.

---

### 23.25 Asynchronous Integrations

Operations that do not require immediate completion may be processed asynchronously.

Examples:

- Large exports
- Email delivery
- Webhook delivery
- Report generation
- External synchronization
- Bulk imports

Possible flow:

```text id="m7q2x8"
API Request
    ↓
Create Job
    ↓
Return Job ID
    ↓
Background Processing
    ↓
Completed / Failed
```

The client can then query job status.

---

### 23.26 Integration Jobs

Background integration jobs should have explicit states.

Example:

```text id="v4r8p2"
PENDING
  ↓
RUNNING
  ↓
COMPLETED
```

Failure path:

```text id="j6m3q9"
RUNNING
  ↓
FAILED
  ↓
RETRYING
  ↓
COMPLETED / PERMANENTLY_FAILED
```

Job execution must preserve tenant context.

---

### 23.27 External Synchronization

Synchronization with external systems must define which system is authoritative for each piece of data.

For example:

```text id="x3p7m5"
WorkFlow → Work Orders
Accounting System → Accounting Records
Payment Provider → Payment Transaction Status
```

The system must avoid ambiguous bidirectional ownership.

Synchronization conflicts should be explicitly handled.

---

### 23.28 Integration Credentials

External credentials must be stored securely.

Examples:

- API keys
- OAuth client secrets
- Webhook signing secrets
- Access tokens
- Refresh tokens

Credentials must:

- Never be committed to Git
- Never appear in normal logs
- Have controlled access
- Be revocable
- Be rotated where appropriate

---

### 23.29 OAuth Integrations

Future integrations may use OAuth.

OAuth credentials should be associated with the correct tenant and integration.

The system should distinguish between:

- WorkFlow application credentials
- Tenant-specific authorization
- Access tokens
- Refresh tokens

Tokens should not be exposed to frontend clients unless the architecture explicitly requires it.

---

### 23.30 Integration Observability

Each important external interaction should be observable.

Useful information includes:

- Integration name
- Operation
- Tenant context where appropriate
- Correlation ID
- Request timestamp
- Duration
- Success/failure
- Retry count
- External event ID
- Error category

Sensitive payloads and credentials must not be logged.

---

### 23.31 Integration Audit

Important integration actions should be auditable.

Examples:

- Payment provider connection
- API credential creation
- API credential revocation
- OAuth authorization
- Webhook configuration
- Webhook delivery failure
- External synchronization
- Integration configuration changes

Audit records must remain tenant-scoped where appropriate.

---

### 23.32 Integration Testing

External integrations should be tested using multiple levels.

#### Unit Tests

Test:

- Adapter logic
- Request mapping
- Response mapping
- Error handling
- Retry decisions

#### Integration Tests

Test against:

- Provider sandbox environments
- Test containers where applicable
- Mock servers

#### Contract Tests

Validate that expected provider API contracts remain compatible.

#### End-to-End Tests

Validate important business workflows involving external systems.

Production providers should not be required for every automated test.

---

### 23.33 Provider Abstraction

The application should avoid unnecessary vendor lock-in.

For example:

```text id="n8q4m6"
EmailService
    ├── ProviderAAdapter
    └── ProviderBAdapter
```

The same principle can apply to:

- Payment providers
- Object storage
- Email
- SMS
- Calendar providers

However, abstractions should only be introduced where they provide meaningful architectural value.

The project should avoid creating excessive generic interfaces before multiple implementations are actually needed.

---

### 23.34 Integration Configuration

Integration configuration should be environment-specific.

Examples:

```text id="q5m8r2"
Development
    ↓
Sandbox Provider

Staging
    ↓
Sandbox Provider

Production
    ↓
Production Provider
```

Production credentials must never be reused in development or test environments.

---

### 23.35 Integration Lifecycle

An integration may have a lifecycle such as:

```text id="p3v7k9"
CONFIGURED
     ↓
CONNECTED
     ↓
ACTIVE
     ↓
DISCONNECTED
     ↓
REVOKED
```

The exact states depend on the integration type.

Integration state must be visible to authorized administrators.

---

### 23.36 Tenant Integration Isolation

Every tenant integration must be associated with exactly the appropriate tenant.

For example:

```text id="r8m4x2"
Tenant A
   ↓
Accounting Integration A

Tenant B
   ↓
Accounting Integration B
```

Tenant A credentials must never be usable to access Tenant B integration data.

Background jobs, caches, events, webhooks, and synchronization processes must preserve this isolation.

---

### 23.37 API Documentation

Public and integration APIs should be documented using OpenAPI where appropriate.

Documentation should include:

- Authentication
- Endpoints
- Request schemas
- Response schemas
- Errors
- Pagination
- Filtering
- Rate limits
- Webhooks
- Idempotency
- Versioning
- Examples

Documentation should evolve together with the API implementation.

---

### 23.38 Integration Deprecation

External APIs and integration features may eventually become obsolete.

Deprecation should be controlled.

The system should provide:

- Deprecation documentation
- Migration guidance
- Defined support periods
- Version information
- Appropriate warnings
- Removal only after the documented lifecycle

Breaking changes should not be introduced without an intentional migration strategy.

---

### 23.39 Business Rules

Initial API and integration rules:

1. External integrations must use explicit, documented contracts.
2. External providers should be isolated behind integration boundaries.
3. Provider-specific code should not unnecessarily spread throughout business modules.
4. Public APIs must be versioned.
5. Tenant API credentials must be tenant-scoped.
6. API credentials must be revocable.
7. API credentials must receive only required permissions or scopes.
8. External credentials must never be committed to source control.
9. External credentials must never be written to normal application logs.
10. Webhook authenticity must be validated.
11. Webhook processing must be idempotent.
12. Duplicate webhook events must not create duplicate business effects.
13. Outbound webhooks must be tenant-scoped.
14. Outbound webhook secrets must be protected.
15. Failed webhook deliveries must use bounded retries.
16. External API calls must have explicit timeouts.
17. Retries must use bounded attempts and appropriate backoff.
18. Only operations safe to retry should be automatically retried.
19. Important retryable operations should support idempotency.
20. External failures must not expose provider-specific internal details unnecessarily.
21. Long-running integrations should be processed asynchronously where appropriate.
22. Background integration jobs must preserve tenant context.
23. Synchronization must define which system is authoritative for each data category.
24. External synchronization must not silently rewrite protected historical records.
25. Integration credentials must be securely stored and controlled.
26. OAuth tokens must be associated with the correct tenant and integration.
27. Integration operations should include appropriate observability information.
28. Sensitive integration payloads must not be logged unnecessarily.
29. Important integration configuration changes must be auditable.
30. External integrations must have appropriate automated tests.
31. Production credentials must be isolated from development and test environments.
32. Tenant integrations must remain isolated across databases, jobs, caches, events, and API operations.
33. API rate limits should protect the platform from excessive external traffic.
34. Public API documentation should remain synchronized with the implemented API.
35. Integration deprecation must use a controlled migration process.
36. Integration abstractions should be introduced where they provide meaningful value and should not create unnecessary complexity.

## 24. Business Rules & State-Machine Governance

WorkFlow SaaS is a business system rather than a collection of unrestricted CRUD operations.

The backend must enforce the rules that determine whether an operation is valid.

Business rules must therefore be implemented independently of frontend behavior and must remain enforced regardless of whether an operation originates from:

- Angular frontend
- Public API
- Internal API
- Background job
- Kafka consumer
- Scheduled task
- Administrative operation
- Future mobile application
- External integration

The frontend may guide users toward valid actions, but the backend remains authoritative.

---

### 24.1 Business Rule Principles

Business rules should be:

- Explicit
- Centralized where practical
- Testable
- Tenant-aware
- Authorization-aware
- Transaction-safe
- Consistent across entry points
- Resistant to concurrent modifications
- Independently testable

Business rules must not exist only inside controllers or frontend components.

---

### 24.2 State Machines

Entities with meaningful lifecycles should use explicit state machines.

Examples include:

- Service Requests
- Work Orders
- Maintenance Plans
- Inventory operations
- Invoices
- Subscriptions
- Notifications
- Integration jobs

A state transition must be validated before changing the persisted state.

For example:

```text id="s7m3q8"
WORK ORDER

DRAFT
  ↓
SCHEDULED
  ↓
ASSIGNED
  ↓
IN_PROGRESS
  ↓
COMPLETED
```

The system must not accept an arbitrary status value simply because it appears in a REST request.

---

### 24.3 Illegal State Transitions

Illegal transitions must be rejected.

For example:

```text id="p4r8n2"
COMPLETED
    ↓
IN_PROGRESS
```

should not be allowed through a normal API operation.

Similarly, the system should reject transitions such as:

```text id="m6q2v9"
CANCELLED → IN_PROGRESS
REJECTED → APPROVED
PAID → DRAFT
DELETED → ACTIVE
```

unless a specifically defined business process permits such a transition.

---

### 24.4 Explicit Transition Operations

Where a transition represents a meaningful business operation, the API should prefer an explicit action.

For example:

```text id="x8k4m1"
POST /api/v1/work-orders/{id}/assign
POST /api/v1/work-orders/{id}/start
POST /api/v1/work-orders/{id}/complete
POST /api/v1/work-orders/{id}/cancel
```

rather than allowing clients to arbitrarily modify:

```text
status = "COMPLETED"
```

This makes business intent explicit and gives the backend a controlled place to validate the operation.

---

### 24.5 Service Request Rules

A Service Request:

- Belongs to exactly one tenant.
- Belongs to a customer within that tenant.
- Has an authorized requester.
- May reference a location.
- May reference equipment.
- May have attachments and comments.
- Has a controlled lifecycle.

If equipment is referenced, the equipment must belong to the same customer and tenant as the request.

A Service Request must not reference resources belonging to another tenant.

---

### 24.6 Service Request Transition Rules

Initial lifecycle:

```text id="v5q9m3"
NEW
 ↓
UNDER_REVIEW
 ↓
APPROVED
 ↓
CONVERTED_TO_WORK_ORDER
```

Alternative terminal states:

```text id="j2r7k8"
UNDER_REVIEW → REJECTED
NEW / UNDER_REVIEW → CANCELLED
```

Only authorized users may perform approval or rejection.

An already converted request must not create another active Work Order through normal processing.

---

### 24.7 Work Order Rules

A Work Order:

- Belongs to exactly one tenant.
- Has a customer within that tenant.
- May originate from a Service Request or Maintenance Plan.
- Has an explicit origin.
- May have technicians assigned.
- May have a schedule.
- Tracks actual execution separately from planned execution.
- Preserves operational history.

A Work Order must not reference a customer, location, equipment, technician, or other resource belonging to another tenant.

---

### 24.8 Work Order Origin

Work Orders can originate from different business processes.

Initial origins:

```text id="k9m4p2"
REACTIVE
    Service Request
        ↓
    Work Order

PREVENTIVE
    Maintenance Plan
        ↓
    Work Order
```

Therefore a Work Order must not require every Work Order to have a `serviceRequestId`.

The origin should be explicitly represented in the domain model.

---

### 24.9 Work Order Transition Rules

Initial lifecycle:

```text id="r6v2m8"
DRAFT
 ↓
SCHEDULED
 ↓
ASSIGNED
 ↓
IN_PROGRESS
 ↓
COMPLETED
```

Additional states:

```text id="n8q3p5"
SCHEDULED / ASSIGNED / IN_PROGRESS
              ↓
           ON_HOLD

DRAFT / SCHEDULED / ASSIGNED / ON_HOLD
              ↓
          CANCELLED

IN_PROGRESS
      ↓
    FAILED
```

The exact legal transition matrix must be implemented explicitly.

---

### 24.10 Completion Rules

A Work Order must satisfy required completion conditions before it can become `COMPLETED`.

Depending on configuration, completion may require:

- Assigned technician
- Actual start time
- Work performed description
- Completion timestamp
- Required measurements
- Required checklist items
- Required photographs
- Required parts information
- Required customer confirmation

The exact requirements may vary by Work Order type.

The backend must validate these requirements rather than relying on frontend form validation.

---

### 24.11 Customer Confirmation

Technician completion and customer confirmation are separate concepts.

Example:

```text id="m3x7q9"
Technician completes work
        ↓
WORK ORDER = COMPLETED
        ↓
Customer confirmation
        ↓
CONFIRMED / REQUIRES_FOLLOW_UP
```

Customer confirmation must not automatically modify technician execution history.

---

### 24.12 Assignment Rules

A technician assignment must validate:

- Technician belongs to the same tenant
- Technician is eligible for the work
- Required skills are satisfied
- Required certifications are satisfied where applicable
- Certification is not expired where validity is required
- Technician availability is compatible
- Scheduling conflicts are considered
- User performing the assignment has permission

The system may warn about certain constraints while allowing an authorized manager to override them where the business rules explicitly permit overrides.

Overrides must be auditable.

---

### 24.13 Scheduling Rules

A schedule must include enough information to identify:

- Work Order
- Planned start
- Planned end
- Timezone
- Scheduling actor
- Relevant notes

The system must reject invalid ranges such as:

```text
plannedEnd < plannedStart
```

Schedule changes must preserve appropriate history.

Actual start and completion timestamps must not replace the original planned schedule.

---

### 24.14 Concurrency in Scheduling

Two users must not unknowingly create conflicting assignments or schedules because they read the same old state.

The system should use appropriate concurrency controls, such as:

- Optimistic locking
- Database constraints
- Transactional checks
- Conflict detection

Example:

```text id="q8m5r3"
Dispatcher A reads schedule
Dispatcher B reads schedule

A assigns technician
B assigns same technician/time

        ↓

Backend detects conflict
        ↓

One operation succeeds
Other receives controlled conflict
```

---

### 24.15 Maintenance Rules

A Maintenance Plan:

- Belongs to one tenant.
- References authorized customer/equipment resources.
- Has a recurrence or defined schedule.
- May define required skills/certifications.
- Can generate Work Orders.
- Preserves maintenance history.

Maintenance generation must be idempotent.

A background job must not create duplicate Work Orders simply because it executes more than once.

---

### 24.16 Inventory Rules

Inventory operations must be explicit.

Stock cannot change merely because a Work Order contains a part description.

A consumption operation must identify:

- Work Order
- Item
- Quantity
- Inventory location
- Actor
- Timestamp

Negative stock must be prevented unless a future business decision explicitly permits it.

Inventory transfers must update both source and destination consistently.

---

### 24.17 Inventory Concurrency

Inventory operations are sensitive to concurrent updates.

Example:

```text id="w4p9k2"
Stock = 5

User A consumes 4
User B consumes 3
```

The system must prevent both operations from successfully consuming stock based on the same stale quantity.

Appropriate transaction and locking strategies must be used.

---

### 24.18 Invoice Rules

Invoices must preserve financial history.

Once an invoice becomes `ISSUED`, important financial fields should not be freely modified.

Initial lifecycle:

```text id="f7m3q8"
DRAFT
 ↓
ISSUED
 ↓
PARTIALLY_PAID
 ↓
PAID
```

Alternative states:

```text id="r5k2n9"
ISSUED → OVERDUE
DRAFT / ISSUED → CANCELLED
```

The exact transition matrix must be explicitly implemented.

---

### 24.19 Financial Immutability

Issued financial records must preserve historical values.

For example:

```text id="x3q8m5"
Work performed
     ↓
Invoice generated
     ↓
Invoice issued
     ↓
Historical price preserved
```

Changing the current Inventory Item price must not silently change an existing invoice.

Corrections should use controlled mechanisms such as:

- Credit notes
- Adjustments
- Replacement invoices
- Corrective records

where appropriate.

---

### 24.20 Subscription Rules

A Tenant Subscription has a controlled lifecycle.

Initial states:

```text id="k6p3v8"
TRIAL
 ↓
ACTIVE
 ↓
PAST_DUE
 ↓
SUSPENDED
 ↓
CANCELLED
```

The exact permitted transitions must be defined.

Subscription state may affect:

- Login
- Feature access
- Usage limits
- API access
- Background processing
- New resource creation

The backend must enforce subscription restrictions.

---

### 24.21 Subscription Usage Limits

Usage limits must be enforced server-side.

Potential limits include:

- Users
- Customers
- Technicians
- Equipment
- Work Orders
- Storage
- API requests
- Reports

The frontend may display usage information, but the backend remains authoritative.

---

### 24.22 Authorization Before Business Rules

Authorization must occur before performing the business operation.

The conceptual sequence is:

```text id="n4m8q2"
Authenticate
    ↓
Identify Tenant
    ↓
Check Membership
    ↓
Check Permission
    ↓
Check Customer Scope
    ↓
Validate Business Rule
    ↓
Execute Transaction
```

Passing a business-rule check must never bypass authorization.

---

### 24.23 Tenant Isolation as a Business Invariant

Tenant isolation is a fundamental invariant.

Every tenant-scoped operation must ensure:

```text id="p7r3m9"
Authenticated Tenant
        ==
Resource Tenant
```

This applies to:

- Reads
- Creates
- Updates
- Deletes
- Searches
- Reports
- Files
- Notifications
- Events
- Cache
- Background jobs
- Exports
- WebSockets

---

### 24.24 Customer Scope

Customer-facing users have an additional authorization boundary.

For example:

```text id="m8q4v2"
Tenant A
 ├── Customer X
 │    └── Customer User X
 │
 └── Customer Y
      └── Customer User Y
```

Customer User X must not access Customer Y's resources.

Tenant administrators and internal users may have broader tenant-level permissions depending on their role.

---

### 24.25 Resource Relationship Validation

Related resources must belong to compatible scopes.

Examples:

```text id="q5m8k3"
Work Order
   ↓
Customer
   ↓
Location
   ↓
Equipment
```

The backend must verify these relationships.

It must not simply accept IDs from the client and assume that the relationships are valid.

---

### 24.26 Historical Integrity

Historical records should preserve important facts about past operations.

Examples:

- Who performed an assignment
- When a schedule changed
- Who completed a Work Order
- Which parts were consumed
- Which price was used on an invoice
- Which user approved a request

Current entity state alone is not sufficient when historical information is required.

---

### 24.27 Auditability of Business Operations

Important state-changing operations should generate appropriate audit information.

Examples:

- Request approval
- Request rejection
- Work Order assignment
- Work Order start
- Work Order completion
- Work Order cancellation
- Inventory adjustment
- Invoice issuance
- Subscription changes
- Role changes

Audit records should identify the actor and relevant context.

---

### 24.28 Transaction Boundaries

Operations that change multiple related records should execute atomically where required.

Example:

```text id="v9p4m6"
Consume Inventory
     +
Add Work Order Part
     +
Create Audit Event
```

If the business operation requires all three changes to succeed together, a transaction should prevent partial completion.

---

### 24.29 Domain Events

Important business events may be published after successful state changes.

Examples:

```text id="c7m2r8"
WorkOrderCompleted
WorkOrderAssigned
ServiceRequestApproved
MaintenanceDue
InvoiceIssued
SubscriptionPaymentFailed
```

Events must represent committed business state.

The architecture should avoid publishing an event that says an operation succeeded when the corresponding database transaction later rolls back.

---

### 24.30 Transactional Outbox

Where reliable event publishing is required, the system should consider the transactional outbox pattern.

Conceptually:

```text id="x6q3n9"
Business Transaction
      ↓
Database State Change
      +
Outbox Event
      ↓
Commit
      ↓
Event Publisher
      ↓
Kafka / External Consumer
```

This reduces the risk of database state and published events becoming inconsistent.

---

### 24.31 Background Job Rules

Background jobs must behave as trusted application operations, not as unrestricted administrators.

Each job must:

- Establish the correct tenant context
- Validate required authorization assumptions
- Operate only on eligible records
- Be idempotent where necessary
- Handle retries safely
- Produce appropriate audit/operational records

A scheduled job must never process records from another tenant accidentally.

---

### 24.32 Idempotency

Operations that may be retried must be designed to avoid duplicate effects.

Examples:

- Payment webhooks
- Maintenance Work Order generation
- Email delivery
- Outbound webhooks
- Inventory processing
- External synchronization
- Background jobs

The implementation may use:

- Idempotency keys
- Unique constraints
- Event identifiers
- Processing records
- Transactional checks

---

### 24.33 Error Classification

Business failures should be distinguishable from infrastructure failures.

Examples:

```text id="r2m7k5"
Invalid state transition
        ↓
Business conflict

Database unavailable
        ↓
Infrastructure failure

Insufficient inventory
        ↓
Business rule violation

External provider timeout
        ↓
Integration failure
```

The API should expose an appropriate error category without exposing sensitive internal details.

---

### 24.34 State Transition History

Important lifecycle transitions should preserve history.

For example:

```text id="n5q8p3"
ASSIGNED
   ↓
IN_PROGRESS
   ↓
ON_HOLD
   ↓
IN_PROGRESS
   ↓
COMPLETED
```

The system should be able to determine:

- Previous state
- New state
- Actor
- Timestamp
- Reason where applicable

This is important for auditing and operational analysis.

---

### 24.35 Business Rule Centralization

Rules should not be duplicated independently across:

- Angular
- Controllers
- Services
- Scheduled jobs
- Kafka consumers
- External integrations

The authoritative business rule must exist in the backend domain/application layer.

Other layers may provide validation or user guidance but must not become the only enforcement point.

---

### 24.36 Configuration-Driven Rules

Some business rules may eventually become configurable.

Examples:

- Required completion fields
- Technician skill requirements
- Maintenance recurrence
- Approval requirements
- Subscription limits
- Customer confirmation requirements

Configurable behavior must still operate within safe system boundaries.

Tenant configuration must never be allowed to disable fundamental security or tenant-isolation rules.

---

### 24.37 Business Rule Testing

Every important business rule should have automated tests.

Tests should cover:

- Valid transitions
- Invalid transitions
- Boundary conditions
- Authorization
- Tenant isolation
- Customer isolation
- Concurrent operations
- Retry behavior
- Idempotency
- Transaction rollback
- Historical integrity

A business rule that cannot be reliably tested is a maintenance risk.

---

### 24.38 Business Rule Documentation

Important business rules should be documented close to the domain implementation.

Documentation should explain:

- Why the rule exists
- What conditions must be satisfied
- Which transitions are allowed
- Which roles can perform the operation
- What happens when the rule fails

This reduces the risk of future developers accidentally weakening an important invariant.

---

### 24.39 Rule Precedence

When multiple rules apply, the system should follow a predictable order.

Recommended conceptual order:

```text id="h3v7m9"
Security
   ↓
Tenant / Resource Scope
   ↓
Authorization
   ↓
State Validity
   ↓
Business Constraints
   ↓
Resource Availability
   ↓
Transaction
```

Security and authorization rules must never be overridden by business configuration.

---

### 24.40 Business Rules Must Be Backend-Enforced

The frontend may:

- Hide unavailable actions
- Disable invalid buttons
- Display validation messages
- Show state-specific workflows

However, none of these are security or business enforcement mechanisms.

The backend must independently validate every important operation.

---

### 24.41 Business Rules and Database Constraints

Application-level validation should be supplemented by database constraints where appropriate.

Examples:

- Unique tenant identifiers
- Unique invoice numbers per tenant
- Unique inventory SKU per tenant
- Foreign-key relationships
- Non-null required fields
- Valid numeric ranges where practical

Database constraints provide a final integrity boundary.

---

### 24.42 Business Rules and Race Conditions

Business rules must remain correct when multiple users or processes operate simultaneously.

Potential race conditions include:

- Two users assigning the same technician
- Two users consuming the same inventory
- Two processes generating the same maintenance Work Order
- Two payment events updating the same subscription
- Two users completing the same Work Order
- Two processes sending the same notification

Concurrency controls must be selected according to the operation.

---

### 24.43 Business Rule Failure Behavior

When a business rule fails:

- The database must remain consistent.
- No partial business operation should remain unless explicitly intended.
- The API should return a meaningful error.
- The failure should be observable where appropriate.
- The user should be able to understand what corrective action is required.
- Sensitive internal details must not be exposed.

---

### 24.44 Business Rule Evolution

Business rules will evolve as the product grows.

Changes should be treated as domain changes rather than simple database-field changes.

When a rule changes, the project should evaluate:

- Existing records
- Existing state transitions
- API compatibility
- Database constraints
- Background jobs
- Events
- Notifications
- Reports
- Historical records
- Existing integrations
- Automated tests

---

### 24.45 Business Rules and Data Migration

If a new rule makes previously valid data invalid, migration planning is required.

Example:

```text id="k4p8m2"
Old rule:
Work Order may complete without technician

New rule:
Work Order requires technician

        ↓

Existing records must be evaluated
        ↓
Migration / exception strategy
```

The system must not blindly apply new constraints to historical data without considering existing records.

---

### 24.46 Business Rules and Versioning

Some business rules may need versioning when historical behavior must remain understandable.

Potential examples:

- Invoice calculation rules
- Subscription limits
- Maintenance generation rules
- Tax configuration
- Notification rules

Historical records should preserve enough information to understand which rules were applied when necessary.

---

### 24.47 Business Rules and Tenant Configuration

Tenant-specific configuration may customize business behavior.

However:

```text id="q8m3v6"
Tenant Configuration
        ↓
Business Customization
```

must never become:

```text id="x5r7n2"
Tenant Configuration
        ↓
Security Bypass
```

Tenant configuration must not disable:

- Tenant isolation
- Authentication
- Authorization
- Audit requirements
- Core data integrity
- Fundamental security controls

---

### 24.48 Business Rules and External Events

External events must never be trusted blindly.

An external event must be:

1. Authenticated where possible
2. Validated
3. Associated with the correct tenant/integration
4. Checked for duplication
5. Converted into an allowed internal operation
6. Audited where appropriate

External systems must not directly dictate arbitrary internal state.

---

### 24.49 State-Machine Governance

Every stateful domain entity should have an explicitly documented transition matrix before implementation.

For each state, the project should define:

- Allowed next states
- Authorized roles
- Required fields
- Required relationships
- Side effects
- Events
- Notifications
- Audit requirements
- Rollback/recovery behavior

This transition matrix becomes a reference for implementation and automated tests.

---

### 24.50 Initial State-Machine Governance Matrix

The project should maintain a dedicated implementation-level matrix covering at least:

| Entity           | States                                                                         | Transition Validation | History     | Events   |
| ---------------- | ------------------------------------------------------------------------------ | --------------------- | ----------- | -------- |
| Service Request  | NEW, UNDER_REVIEW, APPROVED, REJECTED, CONVERTED, CANCELLED                    | Required              | Yes         | Yes      |
| Work Order       | DRAFT, SCHEDULED, ASSIGNED, IN_PROGRESS, ON_HOLD, COMPLETED, FAILED, CANCELLED | Required              | Yes         | Yes      |
| Maintenance Plan | UPCOMING, DUE, OVERDUE, SCHEDULED, COMPLETED, SKIPPED, CANCELLED               | Required              | Yes         | Yes      |
| Invoice          | DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED                        | Required              | Yes         | Yes      |
| Subscription     | TRIAL, ACTIVE, PAST_DUE, SUSPENDED, CANCELLED                                  | Required              | Yes         | Yes      |
| Notification     | Pending/delivery states                                                        | Required              | Appropriate | Optional |
| Integration Job  | PENDING, RUNNING, COMPLETED, FAILED, RETRYING, PERMANENTLY_FAILED              | Required              | Yes         | Yes      |

The exact implementation states may evolve as the domain is implemented.

---

### 24.51 Business Rules — Core Invariants

The following invariants are considered foundational:

1. No user may access resources outside their authorized tenant scope.
2. Customer users may not access resources belonging to another customer unless explicitly authorized.
3. Authorization must be enforced by the backend.
4. Invalid state transitions must be rejected.
5. State cannot be changed by simply submitting an arbitrary status value.
6. Important lifecycle transitions must be explicit and auditable.
7. Work Orders must explicitly identify their business origin.
8. Service Requests must not reference resources from another tenant or customer.
9. Work Orders must not reference resources from another tenant or unauthorized customer.
10. Technician assignments must validate tenant membership.
11. Technician assignments must respect applicable skills and certifications.
12. Expired certifications must not satisfy requirements where current certification is required.
13. Scheduling must reject invalid time ranges.
14. Scheduling conflicts must be detected according to defined business rules.
15. Concurrent scheduling operations must not silently overwrite each other.
16. Inventory consumption must be transactional.
17. Negative stock must be prevented unless explicitly allowed by future requirements.
18. Concurrent inventory operations must not use stale stock quantities.
19. Maintenance Work Order generation must be idempotent.
20. Issued financial records must preserve historical values.
21. Subscription limits must be enforced server-side.
22. Background jobs must execute with the correct tenant context.
23. External events must be validated before affecting internal state.
24. Webhook processing must be idempotent.
25. Important business operations must generate appropriate audit information.
26. Business transactions must not leave inconsistent partial state.
27. Domain events should represent successfully committed business state.
28. Important state transitions must preserve historical information.
29. Business rules must remain enforced regardless of whether the operation originates from the frontend, API, job, event consumer, or integration.
30. Security and tenant-isolation rules must not be disabled through tenant configuration.
31. Database constraints should reinforce critical application-level invariants.
32. Concurrency-sensitive operations must use appropriate locking or optimistic-concurrency mechanisms.
33. Business-rule failures must leave persistent data consistent.
34. Changes to important business rules must consider existing data and migrations.
35. Historical records must remain understandable even when business rules evolve.
36. Each stateful domain entity must have an explicitly documented transition model before implementation.

## 25. Project Development Standards & Coding Conventions

WorkFlow SaaS will follow consistent development standards to keep the codebase maintainable as the system grows.

The standards should encourage clear responsibilities, predictable structure, testability, security, and readability without introducing unnecessary abstractions.

The goal is not to enforce rules for their own sake, but to make the codebase easier to understand, review, test, and evolve.

---

### 25.1 General Principles

Development should follow these principles:

- Prefer clarity over cleverness.
- Keep classes focused on one responsibility.
- Keep business logic out of controllers.
- Avoid unnecessary abstractions.
- Prefer composition over inheritance where appropriate.
- Keep dependencies explicit.
- Validate at system boundaries.
- Make important business rules visible in the domain/application layer.
- Prefer immutable data where practical.
- Avoid duplicated business logic.
- Write code that is easy to test.

---

### 25.2 Java Version

The backend will use a current supported LTS Java version selected at project initialization.

The project should take advantage of modern Java features where they improve clarity and maintainability.

Potential features include:

- Records
- Pattern matching
- Enhanced switch expressions
- `Optional`
- Streams
- Modern collection APIs
- Sealed types where appropriate

Modern language features should not be used merely for novelty.

---

### 25.3 Naming Conventions

Java naming should follow standard Java conventions.

Examples:

```text id="p7m4x2"
Class:
WorkOrderService

Method:
completeWorkOrder()

Variable:
plannedStart

Constant:
MAX_PAGE_SIZE

Package:
com.workflow.workorder
```

Names should communicate domain meaning.

Avoid vague names such as:

```text id="r3q8n5"
Data
Manager
Helper
Processor
Util
Thing
Handler
```

unless their responsibility is genuinely clear from the context.

---

### 25.4 Package Structure

The backend should use domain-oriented packages rather than one large global technical structure.

Preferred structure:

```text id="k8v2m6"
workorder/
├── controller/
├── service/
├── repository/
├── domain/
└── dto/
```

rather than:

```text id="n4p7q3"
controller/
service/
repository/
entity/
dto/
```

containing classes from every business domain.

Domain-oriented organization should make module boundaries easier to understand.

---

### 25.5 Module Boundaries

Major business domains should remain logically separated.

Examples:

```text id="x6m3r9"
identity
tenancy
customer
equipment
service-request
work-order
technician
scheduling
maintenance
inventory
billing
notification
file
audit
reporting
```

A module should expose only what other modules actually need.

Internal implementation details should not become accidental public APIs between modules.

---

### 25.6 Controller Responsibilities

Controllers represent the HTTP boundary.

Controllers should primarily:

- Receive requests
- Validate request structure
- Extract authenticated context
- Call application services
- Return response DTOs

Controllers should not contain substantial business logic.

Avoid:

```text id="q5n8m2"
Controller
    ↓
Validate state
    ↓
Check inventory
    ↓
Change Work Order
    ↓
Create audit record
    ↓
Publish event
```

Instead:

```text id="v7p3k9"
Controller
    ↓
Application Service
    ↓
Domain / Business Logic
    ↓
Persistence / Events
```

---

### 25.7 Service Responsibilities

Application services coordinate business operations.

Examples:

- `CreateServiceRequestService`
- `AssignWorkOrderService`
- `CompleteWorkOrderService`
- `ConsumeInventoryService`

An application service may coordinate:

- Authorization checks
- Domain validation
- Repository operations
- Transactions
- Domain events
- Audit operations

Complex domain rules should not become an enormous service class.

---

### 25.8 Domain Responsibilities

Domain logic should represent business rules.

Examples include:

- Valid Work Order transitions
- Invoice state transitions
- Inventory rules
- Assignment eligibility
- Maintenance generation rules

The domain should not depend unnecessarily on:

- HTTP
- Angular
- Controllers
- Database implementation details
- External providers

This helps keep core business logic testable.

---

### 25.9 Repository Responsibilities

Repositories handle persistence concerns.

They should provide operations required by the domain/application layer without exposing unnecessary persistence details.

Repositories should not become a place for arbitrary business rules.

For example:

```text id="m4q8r2"
Repository:
findActiveWorkOrder(...)

Service / Domain:
Can this Work Order transition to COMPLETED?
```

The repository retrieves data; the business layer determines whether the operation is valid.

---

### 25.10 DTO Standards

API DTOs should be separated from persistence entities.

Typical categories:

```text id="w6p2n9"
CreateRequest
UpdateRequest
Response
SummaryResponse
```

For example:

```text id="c3m8v5"
CreateWorkOrderRequest
UpdateWorkOrderRequest
WorkOrderResponse
WorkOrderSummaryResponse
```

DTOs should expose only fields appropriate for their purpose.

---

### 25.11 JPA Entity Standards

JPA entities represent persistence state.

They should not automatically become API contracts.

Entities should avoid unnecessary business responsibilities that make persistence behavior difficult to reason about.

Relationships should be carefully designed to avoid:

- Accidental eager loading
- Circular serialization
- Large object graphs
- N+1 queries
- Unnecessary database queries

---

### 25.12 Entity and DTO Mapping

Mapping between entities and DTOs should be explicit and testable.

The project may use:

- Manual mapping
- MapStruct
- Another established mapping approach

The choice should prioritize maintainability and compile-time safety where practical.

---

### 25.13 Validation

Validation should happen at appropriate boundaries.

API request validation should use Bean Validation where appropriate.

Examples:

```text id="r8m4k2"
@NotNull
@NotBlank
@Email
@Size
@Positive
@Past
@Future
```

Business validation should remain separate from simple structural validation.

For example:

```text id="x2q7n5"
@NotNull
```

is structural validation.

```text id="p9m3v6"
Work Order cannot be completed while CANCELLED
```

is a business rule.

---

### 25.14 Exception Handling

The API should use centralized exception handling.

A global exception handler should translate expected failures into consistent API responses.

Business errors should use meaningful application error codes.

Example:

```text id="k5r8m3"
WORK_ORDER_INVALID_STATE
TECHNICIAN_UNAVAILABLE
INSUFFICIENT_STOCK
TENANT_ACCESS_DENIED
INVOICE_ALREADY_ISSUED
```

Internal implementation details and stack traces must not be exposed to API consumers.

---

### 25.15 Error Response Structure

API errors should follow the structure defined in the API architecture.

Example:

```json id="u4p7m9"
{
  "code": "WORK_ORDER_INVALID_STATE",
  "message": "The work order cannot be completed from its current state.",
  "timestamp": "...",
  "path": "...",
  "correlationId": "..."
}
```

Validation errors may additionally include field-specific errors.

---

### 25.16 Transaction Standards

Transactions should exist around business operations that require atomicity.

Examples:

- Completing a Work Order
- Consuming inventory
- Issuing an invoice
- Assigning a technician
- Approving a Service Request

Transaction boundaries should represent meaningful business operations rather than arbitrary methods.

---

### 25.17 Transaction Responsibility

Transaction boundaries should generally be controlled at the application/service layer.

A transaction should contain all operations that must succeed or fail together.

Avoid unnecessarily long transactions that hold database resources while performing slow external operations.

---

### 25.18 External Calls and Transactions

External API calls should generally not remain inside long-running database transactions unless specifically required.

For example:

```text id="m7q2v8"
BAD:

Begin DB transaction
    ↓
Call external provider
    ↓
Wait
    ↓
Update database
    ↓
Commit
```

A better architecture may use:

```text id="n4r8p3"
Commit internal state
    ↓
Outbox / Job
    ↓
External provider
    ↓
Retry / reconciliation
```

The exact approach depends on the integration.

---

### 25.19 Logging Standards

Logs should be structured and useful for operations.

Important logs should include information such as:

- Timestamp
- Log level
- Service/module
- Operation
- Correlation ID
- Relevant entity ID
- Safe tenant context
- Error category

Logs must not contain:

- Passwords
- JWTs
- API keys
- Secrets
- Authentication tokens
- Sensitive document contents
- Unnecessary personal information

---

### 25.20 Correlation IDs

Requests should receive a correlation identifier.

The identifier should be propagated through relevant processing layers and asynchronous operations.

This allows an operator to trace:

```text id="q6m3v8"
HTTP Request
    ↓
Application Service
    ↓
Database
    ↓
Outbox
    ↓
Kafka
    ↓
Notification
```

without exposing sensitive business data in the identifier itself.

---

### 25.21 Security Coding Standards

Security must be considered during implementation.

Developers must:

- Never hard-code secrets.
- Never store plaintext passwords.
- Never trust client-supplied tenant IDs.
- Never rely on frontend authorization.
- Validate object ownership/scope.
- Validate external input.
- Avoid unsafe dynamic SQL.
- Protect file uploads.
- Avoid exposing sensitive data through DTOs.
- Avoid logging credentials or tokens.

---

### 25.22 Tenant Context

Tenant context must be established from authenticated identity and membership.

It must not be trusted solely from:

```text id="x8p4m2"
tenantId
```

provided by the frontend.

Every tenant-scoped operation must verify the correct tenant context.

Tenant context must also be preserved across:

- Transactions
- Background jobs
- Kafka events
- Notifications
- Cache operations
- File operations
- Reports
- Exports

---

### 25.23 Dependency Injection

Spring dependency injection should be used consistently.

Constructor injection is preferred.

Example:

```java
@Service
public class WorkOrderService {

    private final WorkOrderRepository repository;

    public WorkOrderService(WorkOrderRepository repository) {
        this.repository = repository;
    }
}
```

Dependencies should be explicit.

Field injection should generally be avoided.

---

### 25.24 Immutability

Immutable objects should be preferred where practical.

Records are appropriate for many:

- DTOs
- Value objects
- API responses
- Event payloads

Mutable JPA entities remain necessary where persistence behavior requires them.

---

### 25.25 Optional Usage

`Optional` should be used primarily for return values where absence is meaningful.

It should not be used indiscriminately for:

- Entity fields
- Method parameters
- Every nullable value

The codebase should use `Optional` where it improves clarity rather than as a replacement for ordinary null handling everywhere.

---

### 25.26 Collections

Collection types should communicate intent.

Examples:

```text id="p3v8m4"
List
Set
Map
```

Use:

- `List` when ordering/duplicates matter
- `Set` when uniqueness matters
- `Map` for key-based lookup

Mutable collections should not be exposed unnecessarily.

---

### 25.27 Streams and Loops

Streams should be used when they make collection processing clearer.

Traditional loops remain appropriate when:

- Complex control flow exists
- Early exits improve readability
- Debugging is easier
- Side effects are unavoidable

The goal is readable code rather than maximizing stream usage.

---

### 25.28 Null Handling

Nullability should be deliberate.

Methods should clearly define whether values may be absent.

Avoid deeply nested null checks where a clearer domain model or `Optional` return value would make the behavior easier to understand.

---

### 25.29 Constants and Magic Values

Repeated business values should not be scattered throughout the code.

Avoid:

```java
if (status.equals("COMPLETED")) {
    ...
}
```

when a domain enum is appropriate.

Prefer:

```java
if (status == WorkOrderStatus.COMPLETED) {
    ...
}
```

Configuration values should be externalized where appropriate.

---

### 25.30 Enums

Enums should represent controlled domain states.

Examples:

```text id="m8q3v6"
WorkOrderStatus
ServiceRequestStatus
InvoiceStatus
SubscriptionStatus
AssignmentStatus
EquipmentStatus
```

State values should not be represented by arbitrary strings throughout the codebase.

---

### 25.31 Comments

Comments should explain **why**, not merely repeat **what** the code does.

Avoid:

```java
// Set status to completed
workOrder.setStatus(COMPLETED);
```

Prefer comments when explaining a non-obvious business or technical decision.

Code should remain readable without excessive commentary.

---

### 25.32 Testing Standards

New business behavior should include appropriate automated tests.

Tests should focus on behavior rather than implementation details.

Important tests include:

- Business rules
- State transitions
- Authorization
- Tenant isolation
- Customer isolation
- Persistence
- API behavior
- Concurrency
- Integration failures
- Idempotency

Critical security and business rules must not depend exclusively on manual testing.

---

### 25.33 Test Naming

Test names should describe the behavior being verified.

For example:

```text id="r5m9q2"
shouldCompleteWorkOrderWhenRequiredFieldsArePresent()

shouldRejectCompletionWhenWorkOrderIsCancelled()

shouldPreventUserFromAccessingAnotherTenantWorkOrder()
```

Tests should make failures understandable without opening the implementation immediately.

---

### 25.34 Test Data

Test data should be deterministic.

Multi-tenant tests should explicitly create separate tenants.

Example:

```text id="v7p2k8"
Tenant A
 ├── User A
 └── Work Order A

Tenant B
 ├── User B
 └── Work Order B
```

Tests must verify that User A cannot access Work Order B.

---

### 25.35 Database Access

Database access should be intentional.

Developers should avoid:

- N+1 queries
- Unnecessary entity loading
- Unbounded result sets
- Fetching entire tables
- Accidental eager relationships

Pagination should be used for potentially large collections.

---

### 25.36 Performance-Aware Development

Performance optimization should be evidence-driven.

The project should not introduce:

- Caching
- Complex query optimization
- Asynchronous processing
- Additional infrastructure

without a demonstrated need or clear architectural requirement.

Measurements should guide optimization.

---

### 25.37 Configuration

Environment-specific configuration must remain outside source-controlled secrets.

Examples:

- Database credentials
- JWT secrets
- API keys
- OAuth credentials
- Payment provider credentials
- Storage credentials

Configuration should be supplied through appropriate environment or secret-management mechanisms.

---

### 25.38 Git Standards

Git commits should describe meaningful changes.

Preferred style:

```text id="x4m8p3"
feat: add work order creation
fix: prevent cross-tenant work order access
test: add technician assignment tests
refactor: extract scheduling service
docs: update API specification
```

Commits should avoid vague messages such as:

```text id="q7r2m5"
changes
stuff
update
fix
work
```

---

### 25.39 Pull Requests

Pull requests should be focused.

A PR should ideally represent a coherent change.

PRs should include:

- What changed
- Why it changed
- Important design decisions
- Testing performed
- Relevant migration information
- Security considerations where applicable

---

### 25.40 Database Migration Standards

Database schema changes must use Flyway migrations.

Migrations should be:

- Versioned
- Reviewable
- Deterministic
- Safe to execute in deployment
- Tested

Existing migrations should not be casually modified after they have been applied to shared environments.

---

### 25.41 Dependency Management

Dependencies should be added only when they provide meaningful value.

Before introducing a dependency, consider:

- Is it necessary?
- Is there already a suitable project capability?
- Is it actively maintained?
- Does it introduce security risk?
- Does it increase application complexity?
- Does its license fit the project?
- Can it be easily removed later?

---

### 25.42 Dependency Updates

Dependencies should be kept reasonably current.

Updates should be tested for:

- Compatibility
- Security
- Runtime behavior
- Database behavior
- API behavior

Security-sensitive dependency updates should receive appropriate priority.

---

### 25.43 Code Review Standards

Code review should focus on:

- Correctness
- Security
- Business rules
- Tenant isolation
- Data integrity
- Test coverage
- Maintainability
- Performance risks
- API compatibility

Reviewers should avoid focusing excessively on subjective formatting when automated tooling can enforce it.

---

### 25.44 Formatting and Static Analysis

The project should eventually use automated tooling for consistent code quality.

Potential tools include:

- Formatter
- Checkstyle
- SpotBugs
- SonarQube/SonarCloud
- Dependency vulnerability scanning

Tools should be introduced incrementally rather than creating unnecessary development friction at the beginning.

---

### 25.45 Architecture Tests

The project may introduce automated architecture tests to protect module boundaries.

Examples:

- Identity should not depend on billing implementation details.
- Domain modules should not directly access unrelated module internals.
- Controllers should not directly access repositories when application services are required.
- API DTOs should not depend on JPA entities.

Architecture tests should enforce genuinely important boundaries.

---

### 25.46 Avoiding Overengineering

The project should avoid implementing infrastructure before it is justified.

Examples of technologies that should not automatically be introduced everywhere:

- Kafka
- Redis
- Complex caching
- Microservices
- Distributed tracing
- Kubernetes
- Multiple databases
- Complex event choreography

The architecture already allows these technologies where they provide real value.

The initial implementation should remain understandable.

---

### 25.47 Technical Debt

Technical debt should be documented rather than silently accumulating.

When a shortcut is intentionally taken, record:

- What was simplified
- Why it was simplified
- Potential consequences
- When it should be revisited

Technical debt should be managed deliberately.

---

### 25.48 Documentation Standards

Documentation should focus on information that helps developers understand and operate the system.

Important documentation may include:

- Architecture
- Database design
- API design
- Deployment
- Security
- Integration behavior
- Development setup
- Operational procedures

Documentation should be updated when meaningful architectural behavior changes.

It should not attempt to describe every line of code.

---

### 25.49 Definition of Done

A backend feature should generally be considered complete when:

- Business rules are implemented.
- Authorization is enforced.
- Tenant isolation is verified.
- Validation is implemented.
- Persistence is correct.
- Required migrations exist.
- Appropriate tests exist.
- API errors are handled consistently.
- Important audit/events are implemented.
- Documentation is updated where necessary.
- The application builds successfully.
- Relevant automated tests pass.

The exact definition may vary depending on the feature.

---

### 25.50 Development Philosophy

The project should follow a practical engineering philosophy:

```text id="n6q3v8"
Understand
   ↓
Design
   ↓
Implement
   ↓
Test
   ↓
Measure
   ↓
Improve
```

The goal is to build a reliable system incrementally.

Architecture should guide implementation, but implementation feedback should also influence architecture.

The project should avoid both extremes:

```text id="r8m4p2"
Under-engineering
        ↕
Over-engineering
```

The preferred approach is deliberate engineering based on actual requirements and evidence.

---

### 25.51 Core Development Rules

The following rules summarize the development standards:

1. Prefer clear code over clever code.
2. Keep classes focused on clear responsibilities.
3. Organize packages around business domains.
4. Keep controllers thin.
5. Keep business rules out of HTTP controllers.
6. Use application services to coordinate business operations.
7. Keep core domain logic independent from infrastructure where practical.
8. Keep repositories focused on persistence.
9. Use DTOs for API contracts.
10. Do not expose JPA entities directly through APIs.
11. Validate input at appropriate boundaries.
12. Centralize API exception handling.
13. Use meaningful business error codes.
14. Define transaction boundaries around atomic business operations.
15. Avoid long database transactions around slow external calls.
16. Use constructor dependency injection.
17. Prefer immutable objects where practical.
18. Use modern Java features when they improve clarity.
19. Use domain enums for controlled states.
20. Avoid unnecessary nullability and ambiguous APIs.
21. Use collections according to their semantic purpose.
22. Use streams when they improve readability, not merely because they exist.
23. Never log passwords, tokens, secrets, or unnecessary sensitive information.
24. Never trust client-supplied tenant context.
25. Enforce authorization on the backend.
26. Write tests around important business behavior.
27. Use deterministic multi-tenant test data.
28. Use Flyway for database schema changes.
29. Keep secrets outside source control.
30. Keep dependencies intentional.
31. Use Git commits that describe meaningful changes.
32. Keep pull requests focused.
33. Use automated formatting and quality tools where beneficial.
34. Protect important architectural boundaries.
35. Avoid premature infrastructure and abstractions.
36. Document meaningful technical debt.
37. Keep documentation useful and maintainable.
38. Define a clear completion standard for features.
39. Prefer evidence-driven performance optimization.
40. Continuously refine the architecture based on implementation experience.

# TSS – Time Sheet System

A JakartaEE web application for managing student assistant contracts and 
weekly/monthly time sheets, built for the *JakartaEE Web Applications 2025* 
university course project.

## System Vision

German minimum wage law (*Mindestlohngesetz*) requires employees earning 
below a certain threshold to document their working hours. At the university, 
this means student assistants must report hours weekly or monthly, with both 
the employee and their supervisor signing off on each time sheet. The 
university must archive these records for two years before deletion.

TSS digitizes this process — currently handled via spreadsheets or paper 
forms — covering contract management, time sheet entry, digital signatures, 
automated reminders, and archiving, all while respecting EU/German data 
protection requirements.

## Roles

| Role | Responsibility |
|---|---|
| **Employee** | Reports worked hours, vacation, and sick leave; signs time sheets |
| **Supervisor** | Contractual employer representative; signs time sheets |
| **Assistant** | Manages contracts and task assignment |
| **Secretary** | Prints and archives signed time sheets |
| **Administrator** | Installs/configures/operates the system |
| **Guest** | Views public information only |

## Core Domain

- **Contract** — defines an employment relationship (supervisor, employee, 
  duration, hours/week). Lifecycle: `PREPARED → STARTED → TERMINATED → ARCHIVED`
- **Timesheet** — one reporting period (week or month) generated automatically 
  once a contract starts. Lifecycle: `IN_PROGRESS → SIGNED_BY_EMPLOYEE → 
  SIGNED_BY_SUPERVISOR → ARCHIVED`
- **TimesheetEntry** — individual work/vacation/sick-leave entries within a 
  time sheet

Full state diagrams and domain model are in [`docs/requirements.pdf`](docs/requirements.pdf).

## Key Features

- Contract CRUD with automatic hours-due / vacation-hours calculation, 
  factoring in public holidays (Rhineland-Palatinate, configurable federal state)
- Time sheet generation, entry management, and dual (employee + supervisor) 
  digital signature workflow
- Daily email reminders for missing entries, signatures, and reports
- Archiving with automatic deletion after 2 years (configurable)
- Role-based access control
- Multi-language UI (English + German minimum)
- Responsive/mobile-friendly UI via PrimeFaces

## Architecture

4-layer JakartaEE architecture:

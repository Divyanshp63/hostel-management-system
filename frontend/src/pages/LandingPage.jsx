import React from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LandingPage = () => {
  const { isAuthenticated, user } = useAuth();

  if (isAuthenticated) {
    if (user?.role === 'STUDENT') {
      return <Navigate to="/student/dashboard" replace />;
    }
    return <Navigate to="/admin/dashboard" replace />;
  }

  return (
    <div className="min-vh-100 d-flex flex-column bg-white">
      {/* Navigation */}
      <header className="navbar navbar-expand-lg border-bottom px-4 py-3 bg-white sticky-top">
        <div className="container-fluid">
          <div className="d-flex align-items-center gap-2">
            <i className="bi bi-buildings-fill fs-3 text-primary"></i>
            <span className="fs-4 fw-bold text-dark">HostelOps</span>
          </div>
          <div className="d-flex align-items-center gap-2">
            <Link to="/login" className="btn btn-outline-primary px-4 fw-medium">
              Sign In
            </Link>
            <Link to="/register" className="btn btn-primary px-4 fw-medium">
              Register
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-5 text-center container my-auto">
        <div className="row py-lg-5 justify-content-center">
          <div className="col-lg-8 col-md-10">
            <span className="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill fw-semibold mb-3">
              Web-Based PG & Hostel Management System
            </span>
            <h1 className="display-4 fw-extrabold text-dark tracking-tight mb-3">
              Next-Gen Automated Hostel Living & Administration
            </h1>
            <p className="lead text-secondary mb-4">
              Complete digital ecosystem with Room Allocation, Online Fee Billing & Payments, 
              Mess Timetable Management, QR & Digital Visitor Passes, and Real-Time Complaints Resolution.
            </p>
            <div className="d-grid gap-2 d-sm-flex justify-content-sm-center">
              <Link to="/login" className="btn btn-primary btn-lg px-4 gap-3 shadow-sm">
                Access Portal <i className="bi bi-arrow-right ms-1"></i>
              </Link>
              <Link to="/register" className="btn btn-outline-secondary btn-lg px-4">
                Student Registration
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Grid */}
      <section className="bg-light py-5 border-top">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="fw-bold">Everything Needed to Run a Smart Hostel</h2>
            <p className="text-muted">Built with Spring Boot 3.3.4, Spring Security, JWT, MySQL & React</p>
          </div>
          <div className="row g-4">
            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm p-3">
                <div className="card-body">
                  <div className="stat-icon primary mb-3">
                    <i className="bi bi-door-open"></i>
                  </div>
                  <h5 className="card-title fw-bold">Room & Bed Allocation</h5>
                  <p className="card-text text-secondary small">
                    Real-time bed availability tracking, automated capacity limits, and one-click student check-in/vacate workflows.
                  </p>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm p-3">
                <div className="card-body">
                  <div className="stat-icon success mb-3">
                    <i className="bi bi-cash-coin"></i>
                  </div>
                  <h5 className="card-title fw-bold">Automated Fee Management</h5>
                  <p className="card-text text-secondary small">
                    Monthly billing generator, partial payment tracking, overdue balance reminders, and instant digital receipts.
                  </p>
                </div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm p-3">
                <div className="card-body">
                  <div className="stat-icon warning mb-3">
                    <i className="bi bi-cup-hot"></i>
                  </div>
                  <h5 className="card-title fw-bold">Mess Menu & Welfare</h5>
                  <p className="card-text text-secondary small">
                    7-day dynamic food calendar for Breakfast, Lunch, Snacks & Dinner, plus online complaint tickets and leave passes.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-4 border-top text-center text-muted small mt-auto">
        &copy; {new Date().getFullYear()} HostelOps Management System &bull; Advanced Java Lab PBL
      </footer>
    </div>
  );
};

export default LandingPage;

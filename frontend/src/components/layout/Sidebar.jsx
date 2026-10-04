import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Sidebar = ({ isMobileOpen, onCloseMobile }) => {
  const { user, isAdmin, isAccountant, isStudent } = useAuth();

  return (
    <aside className={`sidebar ${isMobileOpen ? 'mobile-open' : ''}`}>
      <div className="sidebar-header">
        <i className="bi bi-buildings-fill sidebar-logo"></i>
        <h1 className="sidebar-title">HostelOps</h1>
        {isMobileOpen && (
          <button
            className="btn btn-sm btn-link text-white ms-auto d-lg-none"
            onClick={onCloseMobile}
          >
            <i className="bi bi-x-lg"></i>
          </button>
        )}
      </div>

      <ul className="sidebar-menu">
        {/* ================= ADMIN & ACCOUNTANT NAVIGATION ================= */}
        {(isAdmin() || isAccountant()) && (
          <>
            <li className="sidebar-menu-category">Overview</li>
            <li>
              <NavLink
                to="/admin/dashboard"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-speedometer2"></i>
                <span>Dashboard</span>
              </NavLink>
            </li>

            <li className="sidebar-menu-category">Hostel Administration</li>
            {isAdmin() && (
              <>
                <li>
                  <NavLink
                    to="/admin/students"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-people-fill"></i>
                    <span>Students</span>
                  </NavLink>
                </li>
                <li>
                  <NavLink
                    to="/admin/rooms"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-door-open-fill"></i>
                    <span>Rooms</span>
                  </NavLink>
                </li>
                <li>
                  <NavLink
                    to="/admin/allocations"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-key-fill"></i>
                    <span>Allocations</span>
                  </NavLink>
                </li>
              </>
            )}

            <li className="sidebar-menu-category">Finance & Billing</li>
            <li>
              <NavLink
                to="/admin/fees"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-cash-stack"></i>
                <span>Fees Management</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/admin/payments"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-credit-card-2-front-fill"></i>
                <span>Payments Ledger</span>
              </NavLink>
            </li>

            <li className="sidebar-menu-category">Welfare & Services</li>
            {isAdmin() && (
              <>
                <li>
                  <NavLink
                    to="/admin/complaints"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-tools"></i>
                    <span>Complaints</span>
                  </NavLink>
                </li>
                <li>
                  <NavLink
                    to="/admin/visitors"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-person-badge-fill"></i>
                    <span>Visitor Passes</span>
                  </NavLink>
                </li>
                <li>
                  <NavLink
                    to="/admin/leaves"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-calendar2-week-fill"></i>
                    <span>Leave Requests</span>
                  </NavLink>
                </li>
                <li>
                  <NavLink
                    to="/admin/mess-menu"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-cup-hot-fill"></i>
                    <span>Mess Timetable</span>
                  </NavLink>
                </li>
                <li>
                  <NavLink
                    to="/admin/notices"
                    className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                    onClick={onCloseMobile}
                  >
                    <i className="bi bi-megaphone-fill"></i>
                    <span>Announcements</span>
                  </NavLink>
                </li>
              </>
            )}
          </>
        )}

        {/* ================= STUDENT RESIDENT NAVIGATION ================= */}
        {isStudent() && (
          <>
            <li className="sidebar-menu-category">Student Portal</li>
            <li>
              <NavLink
                to="/student/dashboard"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-grid-1x2-fill"></i>
                <span>My Dashboard</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/student/room"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-door-closed-fill"></i>
                <span>My Room</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/student/fees"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-wallet2"></i>
                <span>My Fees & Pay</span>
              </NavLink>
            </li>

            <li className="sidebar-menu-category">Requests & Services</li>
            <li>
              <NavLink
                to="/student/complaints"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-chat-left-dots-fill"></i>
                <span>Complaints</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/student/leaves"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-airplane-fill"></i>
                <span>Apply for Leave</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/student/visitors"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-person-plus-fill"></i>
                <span>Visitor Request</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/student/mess-menu"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-egg-fried"></i>
                <span>Mess Menu</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/student/notices"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-bell-fill"></i>
                <span>Notices</span>
              </NavLink>
            </li>
            <li>
              <NavLink
                to="/student/profile"
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
                onClick={onCloseMobile}
              >
                <i className="bi bi-person-circle"></i>
                <span>My Profile</span>
              </NavLink>
            </li>
          </>
        )}
      </ul>
    </aside>
  );
};

export default Sidebar;

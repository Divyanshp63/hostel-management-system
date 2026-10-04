import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const Navbar = ({ onToggleSidebar }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleBadgeClass = (role) => {
    switch (role) {
      case 'ADMIN':
        return 'bg-danger';
      case 'ACCOUNTANT':
        return 'bg-warning text-dark';
      case 'STUDENT':
        return 'bg-success';
      default:
        return 'bg-secondary';
    }
  };

  const getUserInitials = (name) => {
    if (!name) return 'U';
    return name
      .split(' ')
      .map((part) => part[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  };

  return (
    <header className="top-navbar">
      <div className="d-flex align-items-center gap-3">
        <button
          className="btn btn-outline-secondary btn-sm d-lg-none"
          onClick={onToggleSidebar}
          aria-label="Toggle Navigation"
        >
          <i className="bi bi-list fs-5"></i>
        </button>
        <span className="text-muted small d-none d-sm-inline">
          Hostel Management System &bull; <strong className="text-dark">Portal</strong>
        </span>
      </div>

      <div className="navbar-user-badge">
        <div className="text-end d-none d-md-block">
          <div className="fw-semibold text-dark small">{user?.name || 'User'}</div>
          <span className={`badge ${getRoleBadgeClass(user?.role)} small`} style={{ fontSize: '0.7rem' }}>
            {user?.role}
          </span>
        </div>

        <div className="user-avatar" title={user?.name}>
          {getUserInitials(user?.name)}
        </div>

        <button
          onClick={handleLogout}
          className="btn btn-sm btn-outline-danger ms-2 d-flex align-items-center gap-1"
          title="Sign out of system"
        >
          <i className="bi bi-box-arrow-right"></i>
          <span className="d-none d-sm-inline">Logout</span>
        </button>
      </div>
    </header>
  );
};

export default Navbar;

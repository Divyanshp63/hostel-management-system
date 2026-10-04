import React from 'react';

const Footer = () => {
  return (
    <footer className="app-footer">
      <div className="container-fluid d-flex flex-column flex-sm-row justify-content-between align-items-center gap-2">
        <div>
          &copy; {new Date().getFullYear()} <strong>HostelOps Management System</strong>. All rights reserved.
        </div>
        <div className="text-muted small">
          Advanced Java PBL Project &bull; Spring Boot 3.3.4 &bull; React 18 &bull; MySQL 8
        </div>
      </div>
    </footer>
  );
};

export default Footer;

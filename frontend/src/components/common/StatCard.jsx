import React from 'react';

const StatCard = ({ title, value, icon, color = 'primary', subtitle }) => {
  return (
    <div className="stat-card">
      <div>
        <h6 className="stat-label">{title}</h6>
        <div className="stat-value">{value}</div>
        {subtitle && <small className="text-muted">{subtitle}</small>}
      </div>
      <div className={`stat-icon ${color}`}>
        <i className={`bi ${icon}`}></i>
      </div>
    </div>
  );
};

export default StatCard;

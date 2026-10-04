import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const MEALS = ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER'];

const StudentMessMenu = () => {
  const [weeklySchedule, setWeeklySchedule] = useState({});
  const [todayMenu, setTodayMenu] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchMessData = async () => {
      try {
        setLoading(true);
        setError('');
        const [weeklyRes, todayRes] = await Promise.all([
          api.get('/api/mess-menu/weekly'),
          api.get('/api/mess-menu/today'),
        ]);

        if (weeklyRes.data?.data?.weeklySchedule) {
          setWeeklySchedule(weeklyRes.data.data.weeklySchedule);
        }
        if (todayRes.data?.data) {
          setTodayMenu(todayRes.data.data);
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to retrieve dining schedule.');
      } finally {
        setLoading(false);
      }
    };

    fetchMessData();
  }, []);

  if (loading) return <LoadingSpinner message="Fetching weekly mess timetable..." />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Mess Timetable & Daily Food Menu</h2>
          <p className="page-subtitle">View today's special culinary preparations and weekly breakfast, lunch & dinner timings</p>
        </div>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}

      {/* Today's Highlight Card */}
      <div className="custom-card mb-4 border-primary">
        <div className="custom-card-header bg-primary text-white d-flex justify-content-between align-items-center">
          <h5 className="custom-card-title text-white mb-0">
            <i className="bi bi-star-fill text-warning me-2"></i> Today's Live Menu ({new Date().toLocaleDateString('en-US', { weekday: 'long' })})
          </h5>
          <span className="badge bg-white text-primary fw-semibold">Current Service</span>
        </div>
        <div className="custom-card-body">
          <div className="row g-3">
            {MEALS.map((mealType) => {
              const currentMeal = todayMenu.find((m) => m.mealType === mealType);
              return (
                <div key={mealType} className="col-12 col-sm-6 col-lg-3">
                  <div className="p-3 bg-light rounded-3 h-100 border">
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <span className={`badge ${
                        mealType === 'BREAKFAST'
                          ? 'bg-warning text-dark'
                          : mealType === 'LUNCH'
                          ? 'bg-primary'
                          : mealType === 'SNACKS'
                          ? 'bg-info text-dark'
                          : 'bg-dark'
                      }`}>
                        {mealType}
                      </span>
                      {currentMeal?.timing && (
                        <small className="text-muted" style={{ fontSize: '0.75rem' }}>
                          {currentMeal.timing}
                        </small>
                      )}
                    </div>
                    <p className="small fw-semibold text-dark mb-1">
                      {currentMeal?.items || 'Not Scheduled'}
                    </p>
                    {currentMeal?.specialDiet && (
                      <small className="text-success d-block">
                        <i className="bi bi-patch-check-fill me-1"></i> {currentMeal.specialDiet}
                      </small>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* 7-Day Weekly Grid */}
      <h4 className="fw-bold text-dark mb-3">Complete 7-Day Timetable</h4>
      <div className="row g-4">
        {DAYS.map((day) => {
          const dayMeals = weeklySchedule[day] || [];
          return (
            <div key={day} className="col-12 col-lg-6">
              <div className="custom-card h-100">
                <div className="custom-card-header bg-light">
                  <h6 className="custom-card-title fw-bold text-dark mb-0">
                    <i className="bi bi-calendar3 text-primary me-2"></i> {day}
                  </h6>
                </div>
                <div className="custom-card-body p-3">
                  <div className="d-flex flex-column gap-2">
                    {MEALS.map((type) => {
                      const meal = dayMeals.find((m) => m.mealType === type);
                      return (
                        <div key={type} className="d-flex justify-content-between align-items-start p-2 rounded-2 bg-light">
                          <div style={{ maxWidth: '75%' }}>
                            <span className="badge bg-secondary me-2 mb-1" style={{ fontSize: '0.7rem' }}>
                              {type}
                            </span>
                            <span className="small fw-medium text-dark">{meal?.items || 'Not scheduled'}</span>
                            {meal?.specialDiet && (
                              <div className="small text-success mt-1">
                                <i className="bi bi-star-fill me-1"></i> {meal.specialDiet}
                              </div>
                            )}
                          </div>
                          {meal?.timing && (
                            <small className="text-muted text-nowrap" style={{ fontSize: '0.75rem' }}>
                              {meal.timing}
                            </small>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default StudentMessMenu;

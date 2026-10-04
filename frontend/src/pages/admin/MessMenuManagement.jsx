import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertMessage from '../../components/common/AlertMessage';

const DAYS_OF_WEEK = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER'];

const MessMenuManagement = () => {
  const [weeklySchedule, setWeeklySchedule] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Upsert Modal
  const [showModal, setShowModal] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formData, setFormData] = useState({
    dayOfWeek: 'MONDAY',
    mealType: 'BREAKFAST',
    items: '',
    timing: '08:00 AM - 09:30 AM',
    specialDiet: '',
  });

  const fetchWeeklyMenu = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/api/mess-menu/weekly');
      if (res.data?.data?.weeklySchedule) {
        setWeeklySchedule(res.data.data.weeklySchedule);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch weekly mess timetable.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWeeklyMenu();
  }, []);

  const handleOpenEdit = (day, mealType, existingMeal) => {
    setFormData({
      dayOfWeek: day,
      mealType: mealType,
      items: existingMeal?.items || '',
      timing: existingMeal?.timing || (mealType === 'BREAKFAST' ? '08:00 AM - 09:30 AM' : mealType === 'LUNCH' ? '12:30 PM - 02:30 PM' : mealType === 'SNACKS' ? '05:00 PM - 06:00 PM' : '08:00 PM - 10:00 PM'),
      specialDiet: existingMeal?.specialDiet || '',
    });
    setShowModal(true);
  };

  const handleSaveMenu = async (e) => {
    e.preventDefault();
    try {
      setFormLoading(true);
      setError('');
      await api.post('/api/mess-menu/upsert', formData);
      setSuccess(`${formData.dayOfWeek} ${formData.mealType} menu updated successfully!`);
      setShowModal(false);
      fetchWeeklyMenu();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save menu.');
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Mess Timetable & Menu Planner</h2>
          <p className="page-subtitle">Configure 7-day dietary schedules, meal timings, and special weekend feasts</p>
        </div>
        <button
          className="btn btn-primary d-flex align-items-center gap-2 shadow-sm"
          onClick={() => {
            setFormData({
              dayOfWeek: 'MONDAY',
              mealType: 'BREAKFAST',
              items: '',
              timing: '08:00 AM - 09:30 AM',
              specialDiet: '',
            });
            setShowModal(true);
          }}
        >
          <i className="bi bi-pencil-square"></i> Update Meal Schedule
        </button>
      </div>

      {error && <AlertMessage type="danger" message={error} onClose={() => setError('')} />}
      {success && <AlertMessage type="success" message={success} onClose={() => setSuccess('')} />}

      {loading ? (
        <LoadingSpinner message="Loading weekly dining timetable..." />
      ) : (
        <div className="row g-4">
          {DAYS_OF_WEEK.map((day) => {
            const dayMeals = weeklySchedule[day] || [];
            return (
              <div key={day} className="col-12 col-xl-6">
                <div className="custom-card h-100">
                  <div className="custom-card-header bg-light">
                    <h5 className="custom-card-title fw-bold text-dark">
                      <i className="bi bi-calendar-check text-primary me-2"></i> {day}
                    </h5>
                  </div>
                  <div className="custom-card-body p-3">
                    <div className="row g-3">
                      {MEAL_TYPES.map((type) => {
                        const meal = dayMeals.find((m) => m.mealType === type);
                        return (
                          <div key={type} className="col-12 col-sm-6">
                            <div className="p-3 border rounded-3 bg-white h-100 d-flex flex-column justify-content-between position-relative">
                              <div>
                                <div className="d-flex justify-content-between align-items-center mb-2">
                                  <span className={`badge ${
                                    type === 'BREAKFAST'
                                      ? 'bg-warning text-dark'
                                      : type === 'LUNCH'
                                      ? 'bg-primary'
                                      : type === 'SNACKS'
                                      ? 'bg-info text-dark'
                                      : 'bg-dark'
                                  }`}>
                                    {type}
                                  </span>
                                  {meal?.timing && (
                                    <small className="text-muted" style={{ fontSize: '0.75rem' }}>
                                      {meal.timing}
                                    </small>
                                  )}
                                </div>
                                <p className="small mb-2 fw-medium text-dark">
                                  {meal?.items || <span className="text-muted fst-italic">Not scheduled</span>}
                                </p>
                                {meal?.specialDiet && (
                                  <small className="badge bg-success-subtle text-success border border-success-subtle d-inline-block">
                                    <i className="bi bi-star-fill me-1"></i> {meal.specialDiet}
                                  </small>
                                )}
                              </div>
                              <button
                                className="btn btn-sm btn-outline-secondary mt-3 w-100"
                                onClick={() => handleOpenEdit(day, type, meal)}
                              >
                                <i className="bi bi-pencil me-1"></i> Edit {type}
                              </button>
                            </div>
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
      )}

      {/* Edit / Upsert Meal Modal */}
      {showModal && (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow">
              <form onSubmit={handleSaveMenu}>
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Configure Menu Item</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Day of Week *</label>
                      <select
                        className="form-select"
                        value={formData.dayOfWeek}
                        onChange={(e) => setFormData({ ...formData, dayOfWeek: e.target.value })}
                      >
                        {DAYS_OF_WEEK.map((d) => (
                          <option key={d} value={d}>{d}</option>
                        ))}
                      </select>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Meal Type *</label>
                      <select
                        className="form-select"
                        value={formData.mealType}
                        onChange={(e) => setFormData({ ...formData, mealType: e.target.value })}
                      >
                        {MEAL_TYPES.map((t) => (
                          <option key={t} value={t}>{t}</option>
                        ))}
                      </select>
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Serving Timings</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. 08:00 AM - 09:30 AM"
                        value={formData.timing}
                        onChange={(e) => setFormData({ ...formData, timing: e.target.value })}
                      />
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Dish & Food Items *</label>
                      <textarea
                        className="form-control"
                        rows="3"
                        placeholder="e.g. Aloo Paratha, Curd, Mixed Pickle, Tea/Coffee"
                        value={formData.items}
                        onChange={(e) => setFormData({ ...formData, items: e.target.value })}
                        required
                      ></textarea>
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Special Diet / Weekend Note</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="e.g. Boiled eggs available on demand / Ice Cream"
                        value={formData.specialDiet}
                        onChange={(e) => setFormData({ ...formData, specialDiet: e.target.value })}
                      />
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={formLoading}>
                    {formLoading ? 'Saving...' : 'Save Meal Schedule'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MessMenuManagement;

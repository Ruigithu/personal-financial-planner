// src/pages/BookAppointment.tsx
import { useEffect, useState } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { format, parse, addDays } from 'date-fns';
import advisorService from '../api/advisor.service';
import financialPlanService from '../api/financial-plan.service';
import appointmentService from '../api/appointment.service';
import oauthService from '../api/oauth.service';
import { Advisor, FinancialPlan, TimeSlot, AppointmentCreateDto } from '@/types';
import {
  Container,
  Typography,
  Grid,
  Paper,
  Box,
  Button,
  TextField,
  CircularProgress,
  Card,
  CardContent,
  RadioGroup,
  Radio,
  FormControlLabel,
  FormControl,
  FormLabel,
  MenuItem,
  Stepper,
  Step,
  StepLabel,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

const sessionTypes = [
  { value: 'INITIAL_CONSULTATION', label: 'Initial Free Consultation (30 min)', duration: 30 },
  { value: 'STANDARD_SESSION', label: 'Standard Paid Session (60 min)', duration: 60 },
  { value: 'FOLLOWUP_SESSION', label: 'Follow-up Session (45 min)', duration: 45 },
  { value: 'PLAN_REVIEW', label: 'Financial Plan Review (60 min)', duration: 60 }
];

const steps = ['Select Session Type', 'Choose Date & Time', 'Confirm Details'];

// OAuth认证状态
enum OAuthState {
  UNKNOWN = 'unknown',
  CHECKING = 'checking',
  AUTHORIZED = 'authorized',
  NOT_AUTHORIZED = 'not_authorized',
  AUTHORIZING = 'authorizing'
}

const BookAppointment = () => {
  const { advisorId } = useParams<{ advisorId: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [advisor, setAdvisor] = useState<Advisor | null>(null);
  const [userPlans, setUserPlans] = useState<FinancialPlan[]>([]);
  const [availableSlots, setAvailableSlots] = useState<TimeSlot[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const [activeStep, setActiveStep] = useState(0);
  const [sessionType, setSessionType] = useState(searchParams.get('type') || 'INITIAL_CONSULTATION');
  const [selectedDate, setSelectedDate] = useState<Date | null>(new Date());
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [selectedPlanId, setSelectedPlanId] = useState<number | ''>('');
  const [notes, setNotes] = useState('');

  // OAuth相关状态
  const [oauthState, setOauthState] = useState<OAuthState>(OAuthState.UNKNOWN);
  const [showOAuthDialog, setShowOAuthDialog] = useState(false);
  const [pendingAppointmentData, setPendingAppointmentData] = useState<AppointmentCreateDto | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (!advisorId) return;

        const [advisorData, plansData] = await Promise.all([
          advisorService.getAdvisorById(parseInt(advisorId)),
          financialPlanService.getUserPlans()
        ]);

        setAdvisor(advisorData);
        setUserPlans(plansData);

        // 检查OAuth状态
        await checkOAuthStatus();

        // If date is selected, fetch available slots
        if (selectedDate) {
          const startDate = format(selectedDate, 'yyyy-MM-dd');
          const endDate = format(addDays(selectedDate, 6), 'yyyy-MM-dd');

          const slotsData = await advisorService.getAvailableTimeSlots(
            parseInt(advisorId),
            startDate,
            endDate
          );
          setAvailableSlots(slotsData);
        }
      } catch (error) {
        toast.error('Failed to load data');
        console.error('Data loading error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [advisorId, selectedDate]);

  // 检查OAuth授权状态
  const checkOAuthStatus = async () => {
    try {
      setOauthState(OAuthState.CHECKING);
      const response = await oauthService.getOAuthStatus();
      setOauthState(response.authorized ? OAuthState.AUTHORIZED : OAuthState.NOT_AUTHORIZED);
    } catch (error) {
      console.error('Failed to check OAuth status:', error);
      setOauthState(OAuthState.NOT_AUTHORIZED);
    }
  };

  // 处理OAuth授权
  const handleOAuthAuthorization = async (appointmentData: AppointmentCreateDto) => {
    try {
      setOauthState(OAuthState.AUTHORIZING);
      setPendingAppointmentData(appointmentData);
      setShowOAuthDialog(true);

      // 创建预约以获取ID
      const tempAppointment = await appointmentService.createAppointment(appointmentData);
      const appointmentId = tempAppointment.id;

      // 获取OAuth授权URL
      const authResponse = await oauthService.getGoogleAuthUrl(appointmentId);

      // 重定向到Google OAuth页面
      window.location.href = authResponse.authUrl;

    } catch (error) {
      console.error('OAuth authorization failed:', error);
      toast.error('Failed to start OAuth authorization');
      setOauthState(OAuthState.NOT_AUTHORIZED);
      setShowOAuthDialog(false);
      setPendingAppointmentData(null);
    }
  };

  const handleDateChange = (date: Date | null) => {
    setSelectedDate(date);
    setSelectedSlot(null);
  };

  const handleNext = () => {
    setActiveStep((prevStep) => prevStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevStep) => prevStep - 1);
  };

  const handleSubmit = async () => {
    if (!advisor || !selectedSlot) return;

    const selectedSession = sessionTypes.find(type => type.value === sessionType);
    if (!selectedSession) return;

    setSubmitting(true);

    try {
      const appointmentDate = parse(
        `${selectedSlot.date} ${selectedSlot.startTime}`,
        'yyyy-MM-dd HH:mm',
        new Date()
      );

      const appointmentData: AppointmentCreateDto = {
        advisorId: advisor.id,
        appointmentDate: appointmentDate.toISOString(),
        durationMinutes: selectedSession.duration,
        sessionType: sessionType as any,
        userNotes: notes
      };

      if (selectedPlanId) {
        appointmentData.sharedPlanId = selectedPlanId as number;
      }

      // 检查OAuth状态
      if (oauthState === OAuthState.NOT_AUTHORIZED) {
        // 用户未授权，启动OAuth流程
        await handleOAuthAuthorization(appointmentData);
        return; // OAuth流程会处理后续逻辑
      }

      // 用户已授权，直接创建预约
      const appointment = await appointmentService.createAppointment(appointmentData);

      toast.success('Appointment booked successfully');
      console.log(appointment);

      // 根据会话类型进行重定向
      if (sessionType !== 'INITIAL_CONSULTATION') {
        navigate(`/appointment/${appointment.id}`);
      } else {
        navigate(`/appointment/${appointment.id}`);
      }
    } catch (error) {
      toast.error('Failed to book appointment');
      console.error('Appointment booking error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  // 处理OAuth完成后的回调
  useEffect(() => {
    const handleOAuthCallback = async () => {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      const state = urlParams.get('state');
      const error = urlParams.get('error');

      if (error) {
        toast.error('OAuth authorization failed');
        setOauthState(OAuthState.NOT_AUTHORIZED);
        return;
      }

      if (code && state) {
        try {
          // 处理OAuth回调
          await oauthService.handleOAuthCallback(code, state);

          toast.success('Google Calendar authorization successful! Your appointment has been created.');
          setOauthState(OAuthState.AUTHORIZED);

          // 清理URL参数
          window.history.replaceState({}, document.title, window.location.pathname);

          // 重定向到预约详情页面
          // 这里需要从回调中获取appointmentId，或者从localStorage中获取
          // 简化起见，重定向到主页
          navigate('/dashboard');

        } catch (error) {
          console.error('OAuth callback error:', error);
          toast.error('Failed to complete authorization');
          setOauthState(OAuthState.NOT_AUTHORIZED);
        }
      }
    };

    handleOAuthCallback();
  }, [navigate]);

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (!advisor) {
    return (
      <Container sx={{ mt: 4 }}>
        <Typography variant="h5">Advisor not found</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Book an Appointment with {advisor.firstName} {advisor.lastName}
      </Typography>

      {/* OAuth状态提示 */}
      {oauthState === OAuthState.NOT_AUTHORIZED && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Google Calendar authorization will be required to generate your meeting link.
        </Alert>
      )}

      {oauthState === OAuthState.AUTHORIZED && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Google Calendar is authorized. Meeting links will be automatically generated.
        </Alert>
      )}

      <Paper sx={{ p: 3, mb: 4 }}>
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {activeStep === 0 && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Select Session Type
            </Typography>

            <FormControl component="fieldset" sx={{ width: '100%' }}>
              <RadioGroup
                value={sessionType}
                onChange={(e) => setSessionType(e.target.value)}
              >
                {sessionTypes.map((type) => (
                  <FormControlLabel
                    key={type.value}
                    value={type.value}
                    control={<Radio />}
                    label={
                      <Box>
                        <Typography variant="subtitle1">{type.label}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          {type.value === 'INITIAL_CONSULTATION'
                            ? 'Free'
                            : `$${advisor.hourlyRate * (type.duration / 60)} (${type.duration} minutes)`}
                        </Typography>
                      </Box>
                    }
                  />
                ))}
              </RadioGroup>
            </FormControl>

            <Box mt={3}>
              <Button
                variant="contained"
                onClick={handleNext}
              >
                Continue
              </Button>
            </Box>
          </Box>
        )}

        {activeStep === 1 && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Select Date and Time
            </Typography>

            <LocalizationProvider dateAdapter={AdapterDateFns}>
              <DatePicker
                label="Appointment Date"
                value={selectedDate}
                onChange={handleDateChange}
                minDate={new Date()}
                disablePast
                slotProps={{ textField: { fullWidth: true, margin: 'normal' } }}
              />
            </LocalizationProvider>

            {selectedDate && (
              <Box mt={3}>
                <Typography variant="subtitle1" gutterBottom>
                  Available Time Slots
                </Typography>

                {availableSlots.length === 0 ? (
                  <Typography variant="body1" color="text.secondary">
                    No available slots for the selected date. Please choose another date.
                  </Typography>
                ) : (
                  <Grid container spacing={2}>
                    {availableSlots
                      .filter(slot => {
                        return slot.date === format(selectedDate, 'yyyy-MM-dd');
                      })
                      .map((slot, index) => (
                        <Grid size={{xs:6,sm:4,md:3}} key={index}>
                          <Button
                            variant={selectedSlot === slot ? 'contained' : 'outlined'}
                            fullWidth
                            onClick={() => setSelectedSlot(slot)}
                          >
                            {format(parse(slot.startTime, 'HH:mm', new Date()), 'h:mm a')}
                          </Button>
                        </Grid>
                      ))}
                  </Grid>
                )}
              </Box>
            )}

            <Box mt={3}>
              <Button
                variant="outlined"
                onClick={handleBack}
                sx={{ mr: 2 }}
              >
                Back
              </Button>
              <Button
                variant="contained"
                onClick={handleNext}
                disabled={!selectedSlot}
              >
                Continue
              </Button>
            </Box>
          </Box>
        )}

        {activeStep === 2 && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Confirm Appointment Details
            </Typography>

            <Grid container spacing={3}>
              <Grid size={{xs:12,sm:6}}>
                <Typography variant="subtitle1">Advisor</Typography>
                <Typography variant="body1">
                  {advisor.firstName} {advisor.lastName}
                </Typography>
              </Grid>

              <Grid size={{xs:12,sm:6}}>
                <Typography variant="subtitle1">Session Type</Typography>
                <Typography variant="body1">
                  {sessionTypes.find(type => type.value === sessionType)?.label}
                </Typography>
              </Grid>

              <Grid size={{xs:12,sm:6}}>
                <Typography variant="subtitle1">Date & Time</Typography>
                <Typography variant="body1">
                  {selectedSlot && selectedDate
                    ? `${format(selectedDate, 'MMMM d, yyyy')} at ${format(parse(selectedSlot.startTime, 'HH:mm', new Date()), 'h:mm a')}`
                    : ''}
                </Typography>
              </Grid>

              <Grid size={{xs:12,sm:6}}>
                <Typography variant="subtitle1">Duration</Typography>
                <Typography variant="body1">
                  {sessionTypes.find(type => type.value === sessionType)?.duration} minutes
                </Typography>
              </Grid>

              <Grid size={{xs:12,sm:6}}>
                <Typography variant="subtitle1">Price</Typography>
                <Typography variant="body1">
                  {sessionType === 'INITIAL_CONSULTATION'
                    ? 'Free'
                    : `$${advisor.hourlyRate * (sessionTypes.find(type => type.value === sessionType)?.duration || 0) / 60}`}
                </Typography>
              </Grid>

              {sessionType === 'PLAN_REVIEW' && (
                <Grid size={{xs:12}}>
                  <TextField
                    select
                    label="Select Financial Plan to Share"
                    value={selectedPlanId}
                    onChange={(e) => setSelectedPlanId(e.target.value as unknown as number)}
                    fullWidth
                    margin="normal"
                  >
                    <MenuItem value="">
                      <em>None (Optional)</em>
                    </MenuItem>
                    {userPlans.map((plan) => (
                      <MenuItem key={plan.id} value={plan.id}>
                        {plan.planName} - Created on {format(new Date(plan.creationDate), 'MMM d, yyyy')}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid>
              )}

              <Grid size={{xs:12}}>
                <TextField
                  multiline
                  rows={4}
                  label="Notes for the Advisor (Optional)"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  fullWidth
                  margin="normal"
                />
              </Grid>
            </Grid>

            <Box mt={3}>
              <Button
                variant="outlined"
                onClick={handleBack}
                sx={{ mr: 2 }}
                disabled={submitting}
              >
                Back
              </Button>
              <Button
                variant="contained"
                color="primary"
                onClick={handleSubmit}
                disabled={submitting || oauthState === OAuthState.CHECKING}
              >
                {submitting || oauthState === OAuthState.AUTHORIZING ? (
                  <>
                    <CircularProgress size={24} sx={{ mr: 1 }} />
                    {oauthState === OAuthState.AUTHORIZING ? 'Authorizing...' : 'Booking...'}
                  </>
                ) : (
                  oauthState === OAuthState.NOT_AUTHORIZED ? 'Authorize & Book' : 'Confirm Booking'
                )}
              </Button>
            </Box>
          </Box>
        )}
      </Paper>

      {/* OAuth授权对话框 */}
      <Dialog open={showOAuthDialog} onClose={() => {}}>
        <DialogTitle>Google Calendar Authorization</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You will be redirected to Google to authorize calendar access.
            This is required to generate your meeting link automatically.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setShowOAuthDialog(false);
            setOauthState(OAuthState.NOT_AUTHORIZED);
            setPendingAppointmentData(null);
          }}>
            Cancel
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default BookAppointment;
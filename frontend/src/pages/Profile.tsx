import React, { useEffect, useState } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import userService from '../api/user.service';
import { UserProfile, UserProfileUpdateDto } from '@/types';
import {
  Container,
  Typography,
  TextField,
  Button,
  Grid,
  Paper,
  Box,
  MenuItem,
  CircularProgress,
  Tabs,
  Tab,
  Divider
} from '@mui/material';

const educationLevels = [
  { value: 'HIGH_SCHOOL', label: 'High School' },
  { value: 'ASSOCIATE', label: 'Associate Degree' },
  { value: 'BACHELOR', label: 'Bachelor\'s Degree' },
  { value: 'MASTER', label: 'Master\'s Degree' },
  { value: 'DOCTORATE', label: 'Doctorate or Higher' }
];

const investmentHorizons = [
  { value: 'SHORT_TERM', label: 'Short Term (< 3 years)' },
  { value: 'MEDIUM_TERM', label: 'Medium Term (3-7 years)' },
  { value: 'LONG_TERM', label: 'Long Term (7+ years)' }
];

const validationSchema = Yup.object().shape({
  age: Yup.number()
    .positive('Age must be positive')
    .integer('Age must be an integer')
    .max(120, 'Age must be less than 120'),
  occupation: Yup.string(),
  educationLevel: Yup.string(),
  monthlyIncome: Yup.number().positive('Monthly income must be positive'),
  monthlyExpenses: Yup.number().positive('Monthly expenses must be positive'),
  totalSavings: Yup.number(),
  totalDebt: Yup.number(),
  riskTolerance: Yup.number().min(1, 'Minimum is 1').max(10, 'Maximum is 10'),
  investmentHorizon: Yup.string(),
  retirementAge: Yup.number().positive('Retirement age must be positive').integer('Retirement age must be an integer'),
  monthlySavings: Yup.number().positive('Monthly savings must be positive')
});

const passwordValidationSchema = Yup.object().shape({
  currentPassword: Yup.string().required('Current password is required'),
  newPassword: Yup.string()
    .required('New password is required')
    .min(8, 'Password must be at least 8 characters')
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
      'Password must contain at least one uppercase letter, one lowercase letter, and one number'
    ),
  confirmPassword: Yup.string()
    .required('Confirm password is required')
    .oneOf([Yup.ref('newPassword')], 'Passwords must match')
});

const Profile = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [tabValue, setTabValue] = useState(0);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await userService.getUserProfile();
        setProfile(data);
      } catch (error) {
        // toast.error('Failed to load profile');
        console.error('Profile error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleProfileUpdate = async (values: UserProfileUpdateDto) => {
    try {

      const updatedProfile = await userService.updateUserProfile(values);
      setProfile(updatedProfile);
      toast.success('Profile updated successfully');
    } catch (error) {
      toast.error('Failed to update profile');
      console.error('Update error:', error);
    }
  };

  const handlePasswordChange = async (values: { currentPassword: string; newPassword: string }) => {
    try {
      await userService.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword
      });
      toast.success('Password changed successfully');
    } catch (error) {
      toast.error('Failed to change password');
      console.error('Password change error:', error);
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Your Profile
      </Typography>

      <Paper sx={{ mb: 4 }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          variant="fullWidth"
        >
          <Tab label="Personal Information" />
          <Tab label="Financial Information" />
          <Tab label="Security" />
        </Tabs>

        <Box p={3}>
          {tabValue === 0 && (
            <Formik
              initialValues={{
                age: profile?.age || 0,
                occupation: profile?.occupation || '',
                educationLevel: profile?.educationLevel || 'HIGH_SCHOOL'
              }}
              validationSchema={validationSchema}
              onSubmit={handleProfileUpdate}
            >
              {({ errors, touched }) => (
                <Form>
                  <Grid container spacing={3}>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="age"
                        label="Age"
                        type="number"
                        error={touched.age && Boolean(errors.age)}
                        helperText={<ErrorMessage name="age" />}
                      />
                    </Grid>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="occupation"
                        label="Occupation"
                        error={touched.occupation && Boolean(errors.occupation)}
                        helperText={<ErrorMessage name="occupation" />}
                      />
                    </Grid>
                    <Grid size={{xs:12}}>
                      <Field
                        as={TextField}
                        select
                        fullWidth
                        name="educationLevel"
                        label="Education Level"
                        error={touched.educationLevel && Boolean(errors.educationLevel)}
                        helperText={<ErrorMessage name="educationLevel" />}
                      >
                        {educationLevels.map((option) => (
                          <MenuItem key={option.value} value={option.value}>
                            {option.label}
                          </MenuItem>
                        ))}
                      </Field>
                    </Grid>
                    <Grid size={{xs:12}}>
                      <Button type="submit" variant="contained" color="primary">
                        Save Changes
                      </Button>
                    </Grid>
                  </Grid>
                </Form>
              )}
            </Formik>
          )}

          {tabValue === 1 && (
            <Formik
              initialValues={{
                monthlyIncome: profile?.monthlyIncome || 0,
                monthlyExpenses: profile?.monthlyExpenses || 0,
                totalSavings: profile?.totalSavings || 0,
                totalDebt: profile?.totalDebt || 0,
                riskTolerance: profile?.riskTolerance || 0,
                investmentHorizon: profile?.investmentHorizon || '',
                retirementAge: profile?.retirementAge || 0,
                monthlySavings: profile?.monthlySavings || 0
              }}
              validationSchema={validationSchema}
              onSubmit={handleProfileUpdate}
            >
              {({ errors, touched }) => (
                <Form>
                  <Grid container spacing={3}>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="monthlyIncome"
                        label="Monthly Income"
                        type="number"
                        InputProps={{ startAdornment: '$' }}
                        error={touched.monthlyIncome && Boolean(errors.monthlyIncome)}
                        helperText={<ErrorMessage name="monthlyIncome" />}
                      />
                    </Grid>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="monthlyExpenses"
                        label="Monthly Expenses"
                        type="number"
                        InputProps={{ startAdornment: '$' }}
                        error={touched.monthlyExpenses && Boolean(errors.monthlyExpenses)}
                        helperText={<ErrorMessage name="monthlyExpenses" />}
                      />
                    </Grid>
                    <Grid  size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="totalSavings"
                        label="Total Savings"
                        type="number"
                        InputProps={{ startAdornment: '$' }}
                        error={touched.totalSavings && Boolean(errors.totalSavings)}
                        helperText={<ErrorMessage name="totalSavings" />}
                      />
                    </Grid>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="totalDebt"
                        label="Total Debt"
                        type="number"
                        InputProps={{ startAdornment: '$' }}
                        error={touched.totalDebt && Boolean(errors.totalDebt)}
                        helperText={<ErrorMessage name="totalDebt" />}
                      />
                    </Grid>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="riskTolerance"
                        label="Risk Tolerance (1-10)"
                        type="number"
                        error={touched.riskTolerance && Boolean(errors.riskTolerance)}
                        helperText={<ErrorMessage name="riskTolerance" />}
                      />
                    </Grid>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        select
                        fullWidth
                        name="investmentHorizon"
                        label="Investment Horizon"
                        error={touched.investmentHorizon && Boolean(errors.investmentHorizon)}
                        helperText={<ErrorMessage name="investmentHorizon" />}
                      >
                        {investmentHorizons.map((option) => (
                          <MenuItem key={option.value} value={option.value}>
                            {option.label}
                          </MenuItem>
                        ))}
                      </Field>
                    </Grid>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="retirementAge"
                        label="Retirement Age"
                        type="number"
                        error={touched.retirementAge && Boolean(errors.retirementAge)}
                        helperText={<ErrorMessage name="retirementAge" />}
                      />
                    </Grid>
                    <Grid size={{xs:12,sm:6}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="monthlySavings"
                        label="Monthly Savings"
                        type="number"
                        InputProps={{ startAdornment: '$' }}
                        error={touched.monthlySavings && Boolean(errors.monthlySavings)}
                        helperText={<ErrorMessage name="monthlySavings" />}
                      />
                    </Grid>
                    <Grid size={{xs:12}}>
                      <Button type="submit" variant="contained" color="primary">
                        Save Changes
                      </Button>
                    </Grid>
                  </Grid>
                </Form>
              )}
            </Formik>
          )}

          {tabValue === 2 && (
            <Formik
              initialValues={{
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
              }}
              validationSchema={passwordValidationSchema}
              onSubmit={handlePasswordChange}
            >
              {({ errors, touched }) => (
                <Form>
                  <Grid container spacing={3}>
                    <Grid size={{xs:12}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="currentPassword"
                        label="Current Password"
                        type="password"
                        error={touched.currentPassword && Boolean(errors.currentPassword)}
                        helperText={<ErrorMessage name="currentPassword" />}
                      />
                    </Grid>
                    <Grid size={{xs:12}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="newPassword"
                        label="New Password"
                        type="password"
                        error={touched.newPassword && Boolean(errors.newPassword)}
                        helperText={<ErrorMessage name="newPassword" />}
                      />
                    </Grid>
                    <Grid size={{xs:12}}>
                      <Field
                        as={TextField}
                        fullWidth
                        name="confirmPassword"
                        label="Confirm Password"
                        type="password"
                        error={touched.confirmPassword && Boolean(errors.confirmPassword)}
                        helperText={<ErrorMessage name="confirmPassword" />}
                      />
                    </Grid>
                    <Grid size={{xs:12}}>
                      <Button type="submit" variant="contained" color="primary">
                        Change Password
                      </Button>
                    </Grid>
                  </Grid>
                </Form>
              )}
            </Formik>
          )}
        </Box>
      </Paper>
    </Container>
  );
};

export default Profile;
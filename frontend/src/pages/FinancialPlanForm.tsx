import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import financialPlanService from '../api/financial-plan.service';
import { FinancialPlanRequest } from '@/types';
import {
  Container,
  Typography,
  TextField,
  Button,
  Paper,
  Box,
  CircularProgress,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import { AxiosError } from 'axios';

const validationSchema = Yup.object().shape({
  planName: Yup.string().required('Plan name is required'),
  additionalInstructions: Yup.string()
});

const steps = ['Profile Confirmation', 'Plan Options', 'Generate Plan'];

const FinancialPlanForm = () => {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(0);
  const [generating, setGenerating] = useState(false);

  const handleNext = () => {
    setActiveStep((prevStep) => prevStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevStep) => prevStep - 1);
  };

  const handleSubmit = async (values: FinancialPlanRequest) => {
    setGenerating(true);
    try {
      const plan = await financialPlanService.generatePlan(values);
      console.log(plan.id);
      toast.success('Financial plan generated successfully');
      navigate(`/plan/${plan.id}`);
    } catch (error:any) {
          toast.error(error.response.data.message);
          console.log('Error response data:', error.response.data.message);
    } finally {
      setGenerating(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Create Financial Plan
      </Typography>

      <Paper sx={{ p: 3, mb: 4 }}>
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        <Formik
          initialValues={{
            planName: '',
            additionalInstructions: ''
          }}
          validationSchema={validationSchema}
          onSubmit={handleSubmit}
        >
          {({ errors, touched, isValid }) => (
            <Form>
              {activeStep === 0 && (
                <Box>
                  <Typography variant="h6" gutterBottom>
                    Confirm Your Profile
                  </Typography>
                  <Typography paragraph>
                    We'll use your profile information to generate a personalized financial plan.
                    Please make sure your profile is up to date before proceeding.
                  </Typography>
                  <Button
                    variant="outlined"
                    onClick={() => navigate('/profile')}
                    sx={{ mr: 2 }}
                  >
                    Update Profile
                  </Button>
                  <Button
                    variant="contained"
                    onClick={handleNext}
                  >
                    Continue
                  </Button>
                </Box>
              )}

              {activeStep === 1 && (
                <Box>
                  <Typography variant="h6" gutterBottom>
                    Plan Options
                  </Typography>
                  <Field
                    as={TextField}
                    fullWidth
                    name="planName"
                    label="Plan Name"
                    error={touched.planName && Boolean(errors.planName)}
                    helperText={<ErrorMessage name="planName" />}
                    margin="normal"
                  />
                  <Field
                    as={TextField}
                    fullWidth
                    multiline
                    rows={4}
                    name="additionalInstructions"
                    label="Additional Instructions (Optional)"
                    error={touched.additionalInstructions && Boolean(errors.additionalInstructions)}
                    helperText={<ErrorMessage name="additionalInstructions" />}
                    margin="normal"
                  />
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
                      disabled={!isValid || !touched.planName}
                    >
                      Continue
                    </Button>
                  </Box>
                </Box>
              )}

              {activeStep === 2 && (
                <Box>
                  <Typography variant="h6" gutterBottom>
                    Generate Your Plan
                  </Typography>
                  <Typography paragraph>
                    We'll use Google AI to analyze your financial information and generate a
                    personalized plan based on your profile and preferences. This may take a minute.
                  </Typography>
                  <Box mt={3}>
                    <Button
                      variant="outlined"
                      onClick={handleBack}
                      sx={{ mr: 2 }}
                      disabled={generating}
                    >
                      Back
                    </Button>
                    <Button
                      type="submit"
                      variant="contained"
                      color="primary"
                      disabled={generating || !isValid}
                    >
                      {generating ? (
                        <>
                          <CircularProgress size={24} sx={{ mr: 1 }} />
                          Generating...
                        </>
                      ) : (
                        'Generate Plan'
                      )}
                    </Button>
                  </Box>
                </Box>
              )}
            </Form>
          )}
        </Formik>
      </Paper>
    </Container>
  );
};

export default FinancialPlanForm;
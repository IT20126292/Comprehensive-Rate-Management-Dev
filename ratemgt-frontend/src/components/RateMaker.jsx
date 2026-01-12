import React, { useState, useEffect, useRef } from 'react';
import { Spinner, ProgressBar } from "react-bootstrap";
import 'bootstrap/dist/css/bootstrap.min.css';
import Swal from 'sweetalert2';
import {
  Table,
  Button,
  Alert,
  Container,
  Row,
  Col,
  Card,
  Modal,
  Form,
  Pagination
} from 'react-bootstrap';
import { format } from 'date-fns';
import {
  fetchCurrenciesByDate,
  searchRateRequests,
  fetchAllRateRequests,
  insertCurrenciesByDate,
  submitRateRequest
} from '../api/api';

// 🔹 helper wrapper for timeout
const withTimeout = (promise, timeout = 50000, errorMsg = '⏳ Request timed out. Please try again.') => {
  return Promise.race([
    promise,
    new Promise((_, reject) => setTimeout(() => reject(new Error(errorMsg)), timeout))
  ]);
};

// small helpers
const norm = (s) => (s ?? '').toString().trim().toUpperCase();
const buildBYOVC_USD = (S, L) => {
  const parts = [];
  if (S != null && S !== '') parts.push(`S - ${S}`);
  if (L != null && L !== '') parts.push(`L - ${L}`);
  return parts.join(' | ');
};

const MakerPage = () => {
  const [selectedDate, setSelectedDate] = useState(format(new Date(), 'dd-MM-yyyy'));
  //const [currencyDate, setSelectedCurrencyDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const datePickerRef = useRef(null);
  const [isDateSelected, setIsDateSelected] = useState(false);
  //const [selectedDate, setSelectedDate] = useState('');
  const [currencies, setCurrencies] = useState([]);
  const [loadingCurrencies, setLoadingCurrencies] = useState(false);
  const [submitStatus, setSubmitStatus] = useState(null);

  const [requests, setRequests] = useState([]);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const [errorRequests, setErrorRequests] = useState(null);

  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showModal, setShowModal] = useState(false);

  // Search filter states
  const [searchDate, setSearchDate] = useState('');
  const [filteredRequests, setFilteredRequests] = useState([]);

  const username = JSON.parse(localStorage.getItem('user')) || 'anonymous';

  // Pagination states
  const [currentCurrencyPage, setCurrentCurrencyPage] = useState(1);
  const currenciesPerPage = 5;

  const [currentModalPage, setCurrentModalPage] = useState(1);
  const itemsPerModalPage = 5;
  
  //newl added 
  const [progress, setProgress] = useState(0);
  const [progressText, setProgressText] = useState("");
  const [isFetchDisabled, setIsFetchDisabled] = useState(false);
  const [isSubmitDisabled, setIsSubmitDisabled] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const getISTTimestamp = () => {
  const date = new Date();

  // Convert current UTC time to IST time
  const istDate = new Date(date.toLocaleString("en-US", { timeZone: "Asia/Kolkata" }));

  // Extract components
  const year = istDate.getFullYear();
  const month = String(istDate.getMonth() + 1).padStart(2, "0");
  const day = String(istDate.getDate()).padStart(2, "0");
  const hours = String(istDate.getHours()).padStart(2, "0");
  const minutes = String(istDate.getMinutes()).padStart(2, "0");

  // Return formatted string
  const formattedTime = `${year}-${month}-${day} ${hours}:${minutes}`;
  return String(formattedTime);
};

const getISTLocalDateTime = () => {
  const now = new Date();

  // Convert to IST manually
  const utc = now.getTime() + now.getTimezoneOffset() * 60000;
  const istTime = new Date(utc + 5.5 * 60 * 60 * 1000);

  // Format to yyyy-MM-dd'T'HH:mm:ss (no milliseconds, no timezone)
  const pad = (n) => (n < 10 ? '0' + n : n);
  const formatted = `${istTime.getFullYear()}-${pad(istTime.getMonth() + 1)}-${pad(istTime.getDate())}T${pad(istTime.getHours())}:${pad(istTime.getMinutes())}:${pad(istTime.getSeconds())}`;

  return formatted;
};

  useEffect(() => {
    setCurrentModalPage(1);
  }, [selectedRequest]);

  // Detect date selection to enable button
  useEffect(() => {
    const input = datePickerRef.current;
    setIsFetchDisabled(false);
    if (!input) return;

    const handleInputChange = () => {
      setIsDateSelected(!!input.value);
    };

    input.addEventListener('change', handleInputChange);
    return () => input.removeEventListener('change', handleInputChange);
  }, []);

  // Helper to format date as yyyy-mm-dd
  const formatDateForOracle = (isoDate) => {
    if (!isoDate) return null;
    const [year, month, day] = isoDate.split('-');
    return `${day}-${month}-${year}`; // YYYY-MM-DD format
  };

  // Initialize filteredRequests when requests change
  useEffect(() => {
    setFilteredRequests(requests);
  }, [requests]);

  // 🔁 Instant filter handler
  const handleInstantFilter = async (dateValue) => {
    console.log("Date Picker is working for date : " + dateValue);
    setSearchDate(dateValue);
    console.log("In search");
    if (dateValue != null) {
      console.log("In IN search dateValue");
      const searchByDateRequest = await searchRateRequests(dateValue);
      console.log("Searc By Date Request :");
      console.log(searchByDateRequest.data);
      setFilteredRequests(searchByDateRequest.data); // reset if cleared
      return;
    }

    const filtered = requests.filter((req) => {
      if (!req.requestedAt) return false;

      const formattedReqDate = format(new Date(req.requestedAt), 'yyyy-MM-dd');
      return formattedReqDate === dateValue;
    });

    setFilteredRequests(filtered);
  };

  // 🔹 Fetch currencies with timeout
  // const fetchCurrencies = async (date) => {
  //   setLoadingCurrencies(true);
  //   try {
  //     const res = await withTimeout(fetchCurrenciesByDate(date));
  //     console.log("RTl :");
  //     console.log(res.data);
  //     setCurrencies(res.data);
  //     setCurrentCurrencyPage(1);
  //   } catch (err) {
  //     console.error(err);
  //     setSubmitStatus({ type: 'danger', message: err.message || '❌ Failed to load currencies.' });
  //   } finally {
  //     setLoadingCurrencies(false);
  //   }
  // };

  //newly added
  const fetchCurrencies = async (date) => {
  setLoadingCurrencies(true);
  setSubmitStatus(null);
  setProgress(0);
  setProgressText(`Starting currency sync for ${date}...`);
  setCurrencies([]);
  setIsFetchDisabled(true); // disable fetch while syncing

  try {
    setProgressText("⏳ Fetching and inserting currency data...");
    await insertCurrenciesByDate(date);

    const maxAttempts = 1;
    let attempts = 0;
    let currenciesData = [];

    while (attempts < maxAttempts) {
      attempts++;
      const progressPercent = Math.round((attempts / maxAttempts) * 100);
      setProgress(progressPercent);
      setProgressText(`⏳ Checking data availability...`);

      const res = await fetchCurrenciesByDate(date);
      //console.log(`Polling attempt #${attempts}`, res.data);

      if (res.data && res.data.length > 0) {
        currenciesData = res.data;
        setProgress(100);
        setProgressText(`✅ Data ready after ${attempts} attempts!`);
        break;
      }

      await new Promise((resolve) => setTimeout(resolve, 5000));
    }

    if (currenciesData.length === 0) {
      throw new Error("❌ No data found for selected date");
    }

    setCurrencies(currenciesData);
    setSubmitStatus({
      type: "success",
      message: `🔍 ${currenciesData.length} currency rate combinations fetched successfully at ${getISTTimestamp()}`,
    });
    console.log("Submit enabling");
    if(currenciesData.length > 0){
      setIsSubmitDisabled(false);
    }

  } catch (err) {
    console.error("❌ Error fetching currencies:", err);
    setSubmitStatus({
      type: "danger",
      message: err.message + ". Failed to Proceed." || "❌ Failed to fetch currency rate combinations.",
    });

    // Re-enable fetch button if it failed
    setIsFetchDisabled(false);
  } finally {
    setLoadingCurrencies(false);
    setProgressText("");
  }
};


  // 🔹 Fetch requests with timeout
  const fetchRequests = async () => {
    setLoadingRequests(true);
    try {
      const res = await withTimeout(fetchAllRateRequests());
      const userRequests = res.data.filter(r => r.requestedBy === username);
      setRequests(userRequests);
      setErrorRequests(null);
    } catch (err) {
      console.error(err);
      setErrorRequests(err.message || 'Failed to load requests.');
    } finally {
      setLoadingRequests(false);
    }
  };

  useEffect(() => {
    fetchCurrencies(selectedDate);
  }, [selectedDate]);

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleDateChange = (e) => {
    setIsFetchDisabled(false);
    const value = e.target.value;
    setSelectedDate(value);
   
};
const enableFetchButton = (e) => {
    setIsFetchDisabled(false);
    setIsSubmitDisabled(true);
};

const validateRejection = async () => {
    let result = 0;
    const confirmed = await Swal.fire({
      title: "Confirm Request?",
      text: "Do you want to submit this request for confirmation",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Yes, Confirm",
      cancelButtonText: "No, Cancel",
      confirmButtonColor: "#198754", // red button
      cancelButtonColor: "#6c757d" // gray button
    });
    if (!confirmed.isConfirmed) {
      console.log("User cancelled rejection");
      result = 1; // stop flow
    }
    return result; // 0 = OK, 1 = STOP
  };

  //  Submit with timeout
const handleSubmit = async () => {
  
    if (currencies.length === 0) {
      setSubmitStatus({ type: 'danger', message: 'No currencies to submit.' });
      return;
    }
    let decision = await validateRejection();
    
    if(decision === 0){
      setIsSubmitting(true); // 🆕 show spinner
      setIsSubmitDisabled(true); // 🆕 disable button immediately
      const rateRequestPayload = {
        requestedBy: username,
        requestedAt: getISTLocalDateTime(),
        status: 'IN_PROGRESS',
        rateItems: currencies.map((c) => ({
          fixCurrencyCode: c.id.fxdCrncyCode,
          varCurrencyCode: c.id.varCrncyCode,
          rateCode: c.id.rateCode,
          varCurnyUnit: c.varCrncyUnit
        }))
      };
      try {
        await withTimeout(submitRateRequest(rateRequestPayload));
        setSubmitStatus({ type: 'success', message: '✅ Rate request submitted successfully.' });
        fetchRequests();
        
      } catch (err) {
        console.error(err);
        setSubmitStatus({ type: 'danger', message: err.message || '❌ Failed to submit rate request.' });
      } finally {
        setIsSubmitting(false);
      }  
    }
  };

  const handleOpenRequest = (request) => {
    setSelectedRequest(request);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setSelectedRequest(null);
    setShowModal(false);
  };

   // -------- Group currencies first (FXD=USD ⇒ RE_BYOVC = "S - … | L - …") ----------
  const getGroupedCurrencies = () => {
    const grouped = {};
    currencies.forEach(currency => {
      const key = `${currency.id.fxdCrncyCode}-${currency.id.varCrncyCode}`;
      if (!grouped[key]) {
        grouped[key] = {
          fxdCrncyCode: currency.id.fxdCrncyCode,
          varCrncyCode: currency.id.varCrncyCode,
          RE_TTBUY: "",
          RE_ODBUY: "",
          RE_TTSEL: "",
          RE_BYOVC: "",
          // temp holders to build "S - … | L - …" for USD (fixed)
          _CBCL: null, // L
          _CBCS: null  // S
        };
      }
      switch (currency.id.rateCode) {
        case "TTBY": grouped[key].RE_TTBUY = currency.varCrncyUnit; break;
        case "ODBY": grouped[key].RE_ODBUY = currency.varCrncyUnit; break;
        case "TTSL": grouped[key].RE_TTSEL = currency.varCrncyUnit; break;
        case "CBCL": grouped[key]._CBCL = currency.varCrncyUnit; break; // L
        case "CBCS": grouped[key]._CBCS = currency.varCrncyUnit; break; // S
        default: break; // CNSR (RE_SLOVC) removed
      }
    });

    return Object.values(grouped).map((row) => {
      if (norm(row.fxdCrncyCode) === 'USD') {
        row.RE_BYOVC = buildBYOVC_USD(row._CBCS, row._CBCL); // "S - … | L - …"
      } else {
        // Keep original behavior for non-USD: show CBCL only if present
        row.RE_BYOVC = row._CBCL ?? row.RE_BYOVC ?? '';
      }
      delete row._CBCL;
      delete row._CBCS;
      return row;
    });
  };

  /* -------- Paginate after grouping ----------
  const getPaginatedGroupedCurrencies = () => {
    const groupedItems = getGroupedCurrencies();
    const start = (currentCurrencyPage - 1) * currenciesPerPage;
    const end = currentCurrencyPage * currenciesPerPage;
    return groupedItems.slice(start, end);
  }; */

  const groupedCurrencies = getGroupedCurrencies();

  const totalCurrencyPages = Math.ceil(getGroupedCurrencies().length / currenciesPerPage);

  // -------- Group + paginate rateItems inside modal (FXD=USD ⇒ "S - … | L - …") ----------
  const getGroupedRateItems = () => {
    if (!selectedRequest || !selectedRequest.rateItems) return [];
    const grouped = {};
    selectedRequest.rateItems.forEach(item => {
      if (!grouped[item.fixCurrencyCode]) {
        grouped[item.fixCurrencyCode] = {
          fixCurrencyCode: item.fixCurrencyCode,
          varCurrencyCode: item.varCurrencyCode,
          RE_TTBUY: "",
          RE_ODBUY: "",
          RE_TTSEL: "",
          RE_BYOVC: "",
          _CBCL: null, // L
          _CBCS: null  // S
        };
      }
      switch (item.rateCode) {
        case "TTBY": grouped[item.fixCurrencyCode].RE_TTBUY = item.varCurnyUnit; break;
        case "ODBY": grouped[item.fixCurrencyCode].RE_ODBUY = item.varCurnyUnit; break;
        case "TTSL": grouped[item.fixCurrencyCode].RE_TTSEL = item.varCurnyUnit; break;
        case "CBCL": grouped[item.fixCurrencyCode]._CBCL = item.varCurnyUnit; break; // L
        case "CBCS": grouped[item.fixCurrencyCode]._CBCS = item.varCurnyUnit; break; // S
        default: break; // CNSR (RE_SLOVC) removed
      }
    });

    return Object.values(grouped).map((row) => {
      if (norm(row.fixCurrencyCode) === 'USD') {
        row.RE_BYOVC = buildBYOVC_USD(row._CBCS, row._CBCL); // "S - … | L - …"
      } else {
        row.RE_BYOVC = row._CBCL ?? row.RE_BYOVC ?? '';
      }
      delete row._CBCL;
      delete row._CBCS;
      return row;
    });
  };

  const getPaginatedGroupedRateItems = () => {
    const groupedItems = getGroupedRateItems();
    const start = (currentModalPage - 1) * itemsPerModalPage;
    const end = currentModalPage * itemsPerModalPage;
    return groupedItems.slice(start, end);
  };

  const totalModalPages = Math.ceil(getGroupedRateItems().length / itemsPerModalPage);

  return (
    <Container fluid className="pt-5"
      style={{ paddingLeft: '300px', backgroundColor: '#f8f9fa', minHeight: '100vh', marginTop: '60px' }}>
      <Row className="mb-4">
        <Col>
          <h5 className="text-primary fw-bold text-center">Currency Rates Request</h5>
        </Col>
      </Row>

      {/* Currencies Table Section */}
      <Row className="justify-content-center mb-5">
        <Col md={10}>
          <Card className="shadow rounded-4 border-0">
            <Card.Body className="p-4">

              {submitStatus && (
                <Alert variant={submitStatus.type} dismissible>
                  {submitStatus.message}
                </Alert>
              )}

              <Form.Group controlId="datePicker" className="mb-3">
                <Form.Label>Select Date</Form.Label>
                <Form.Control
                  ref={datePickerRef}
                  type="date"
                  onChange={enableFetchButton}
                />
              </Form.Group>

              {loadingCurrencies ? (
                <div className="mt-3 text-center">
                  <Spinner animation="border" role="status" />
                  <p className="mt-2">{progressText || "Loading currencies..."}</p>
                  <ProgressBar
                    now={progress}
                    animated
                    striped
                    variant="info"
                    label={`${progress}%`}
                    style={{ height: "20px", width: "60%", margin: "auto" }}
                  />
                </div>
                
              ) : (
                 <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th rowSpan="2" className="align-middle text-center">FIXED CURRENCY</th>
                      {/* <th rowSpan="2" className="align-middle text-center">VAR CURRENCY</th> */}
                      <th colSpan="3" className="text-center">EXCHANGE RATES</th>
                      <th colSpan="2" className="text-center">CURRENCY RATES</th>
                    </tr>
                    <tr>
                    <th className="text-center">T/T BUYING RATE</th>
                    <th className="text-center">O/D BUYING RATE</th>
                    <th className="text-center">T/T SELLING RATE</th>
                    <th className="text-center">BUY OTC<br></br>RATE</th>
                    </tr>
                  </thead>
                  <tbody>
                    {groupedCurrencies.length > 0 ? (
                      groupedCurrencies.map((row, idx) => (
                        <tr key={idx}>
                          <td >{row.fxdCrncyCode}</td>
                          {/* <td>{row.varCrncyCode}</td> */}
                          <td className="text-end">{row.RE_TTBUY}</td>
                          <td className="text-end">{row.RE_ODBUY}</td>
                          <td className="text-end">{row.RE_TTSEL}</td>
                          <td className="text-end">{row.RE_BYOVC}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="7" className="text-center">❌ No data found for selected date.</td>
                      </tr>
                    )}
                  </tbody>
                </Table>
                
              )}

              <Row className="mt-4">
                <Col className="text-start">
                  <Button
                    variant="primary" // same style as Submit button
                    onClick={() => {
                      const dateValue = datePickerRef.current?.value;
                      if (!dateValue) return;

                      const formattedDate = formatDateForOracle(dateValue);
                      setSelectedDate(formattedDate);
                      handleDateChange({ target: { value: formattedDate } });
                    }}
                    disabled={loadingCurrencies || !isDateSelected || isFetchDisabled}
                  >
                    {loadingCurrencies ? 'Fetching...' : 'Extract Data'}
                  </Button>
                </Col>

                <Col className="text-end">
                  
                <Button
                  variant="primary"
                  onClick={handleSubmit}
                  disabled={currencies.length === 0 || loadingCurrencies || isSubmitDisabled || isSubmitting}
                >
                  {isSubmitting ? (
                    <>
                      <Spinner animation="border" size="sm" className="me-2" /> Confirming...
                    </>
                  ) : (
                    "Confirm"
                  )}
                </Button>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Submitted Requests List */}
      <Row className="justify-content-center">
        <Col md={10}>
          <Card className="shadow rounded-4 border-0">
            <Card.Header className="bg-primary text-white fw-semibold fs-10 rounded-top">
              Submitted Rate Requests
            </Card.Header>

            <Card.Body className="p-4">
              {errorRequests && <Alert variant="danger">{errorRequests}</Alert>}

              {/* 🔍 Instant Filter Section */}
              <Form.Group as={Row} className="mb-4 align-items-center">
                <Col sm="3">
                  <Form.Control
                    type="date"
                    value={searchDate}
                    onChange={(e) => handleInstantFilter(e.target.value)}
                    disabled={loadingRequests}
                  />
                </Col>
              </Form.Group>

              {loadingRequests ? (
                <p>Loading submitted requests...</p>
              ) : filteredRequests.length === 0 ? (
                <p>No requests found for selected date.</p>
              ) : (
                <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Requested At</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredRequests.map((req) => (
                      <tr key={req.id}>
                        <td>{req.id}</td>
                        <td>
                          {req.requestedAt
                            ? format(new Date(req.requestedAt), 'yyyy-MM-dd HH:mm')
                            : 'N/A'}
                        </td>
                        <td>{req.status}</td>
                        <td>
                          <Button
                            size="sm"
                            variant="primary"
                            onClick={() => handleOpenRequest(req)}
                          >
                            View
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Modal */}
      <Modal show={showModal} onHide={handleCloseModal} size="lg" centered>
        <Modal.Header closeButton className="bg-primary text-white fw-semibold fs-10 rounded-top">
          <Modal.Title><h5>Rate Request Details</h5></Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedRequest ? (
            <>
              <p><strong>Requested By:</strong> {selectedRequest.requestedBy}</p>
              <p><strong>Requested At:</strong> {selectedRequest.requestedAt ? format(new Date(selectedRequest.requestedAt), 'yyyy-MM-dd HH:mm') : 'N/A'}</p>
              <p><strong>Status:</strong> {selectedRequest.status}</p>

              <Table striped bordered hover responsive>
                <thead>
                  <tr>
                    <th rowSpan="2" className="align-middle text-center">FIXED CURRENCY CODE</th>
                    {/* <th rowSpan="2" className="align-middle text-center">VAR CURRENCY CODE</th> */}
                    <th colSpan="3" className="text-center">
                      EXCHANGE RATES
                    </th>
                    <th colSpan="2" className="text-center">
                      CURRENCY RATES
                    </th>
                  </tr>
                  <tr>
                    <th className="text-center">T/T BUYING RATE</th>
                    <th className="text-center">O/D BUYING RATE</th>
                    <th className="text-center">T/T SELLING RATE</th>
                    <th className="text-center">BUY OTC<br></br>RATE</th>
                  </tr>
                </thead>
                <tbody>
                  {getPaginatedGroupedRateItems().length > 0 ? (
                    getPaginatedGroupedRateItems().map((row, idx) => (
                      <tr key={idx}>
                        <td>{row.fixCurrencyCode}</td>
                        {/* <td>{row.varCurrencyCode}</td> */}
                        <td className="text-end">{row.RE_TTBUY}</td>
                        <td className="text-end">{row.RE_ODBUY}</td>
                        <td className="text-end">{row.RE_TTSEL}</td>
                        <td className="text-end">{row.RE_BYOVC}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="7" className="text-center">❌ No rate items found.</td>
                    </tr>
                  )}
                </tbody>
              </Table>

              {totalModalPages > 1 && (
                <Pagination className="justify-content-center mt-3">
                  <Pagination.First
                    onClick={() => setCurrentModalPage(1)}
                    disabled={currentModalPage === 1}
                  />
                  <Pagination.Prev
                    onClick={() => setCurrentModalPage(prev => Math.max(prev - 1, 1))}
                    disabled={currentModalPage === 1}
                  />
                  {Array.from({ length: totalModalPages }).map((_, idx) => (
                    <Pagination.Item
                      key={idx + 1}
                      active={currentModalPage === idx + 1}
                      onClick={() => setCurrentModalPage(idx + 1)}
                    >
                      {idx + 1}
                    </Pagination.Item>
                  ))}
                  <Pagination.Next
                    onClick={() => setCurrentModalPage(prev => Math.min(prev + 1, totalModalPages))}
                    disabled={currentModalPage === totalModalPages}
                  />
                  <Pagination.Last
                    onClick={() => setCurrentModalPage(totalModalPages)}
                    disabled={currentModalPage === totalModalPages}
                  />
                </Pagination>
              )}
            </>
          ) : (
            <p>❌ No details to show.</p>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCloseModal}>Close</Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default MakerPage;
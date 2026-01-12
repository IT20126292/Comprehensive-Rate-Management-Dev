import React, { useState, useEffect } from 'react';
import { Spinner } from 'react-bootstrap';
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
  approveRejectRateRequest,
  fetchAllRateRequests,
  searchRateRequests,
  updateOutputCurrencyUnits
} from '../api/api.js';

// 🔹 helper wrapper for timeout
const withTimeout = (promise, timeout = 50000, errorMsg = '⏳ Request timed out. Please try again.') => {
  return Promise.race([
    promise,
    new Promise((_, reject) => setTimeout(() => reject(new Error(errorMsg)), timeout))
  ]);
};

const norm = (s) => (s ?? '').toString().trim().toUpperCase();
const buildBYOVC_USD = (S, L) => {
 const parts = [];
 if (S != null && S !== '') parts.push(`S - ${S}`);
 if (L != null && L !== '') parts.push(`L - ${L}`);
 return parts.join(' | ');
};

const CheckerPage = () => {
  const [requests, setRequests] = useState([]);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const [errorRequests, setErrorRequests] = useState(null);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [decisionComment, setDecisionComment] = useState('');
  const [modalMessage, setModalMessage] = useState(null);
  const [lockActions, setLockActions] = useState(false); // ✅ disables Approve/Reject + textarea
  const [isSubmittingDecision, setIsSubmittingDecision] = useState(false);
  const [suppressAutoOpen, setSuppressAutoOpen] = useState(false);

  // 🔍 Search filter states
  const [searchDate, setSearchDate] = useState('');

  const username = JSON.parse(localStorage.getItem('user')) || 'anonymous';

  // Modal Pagination
  const [currentModalPage, setCurrentModalPage] = useState(1);
  const itemsPerModalPage = 5;

  // 🔍 Instant Search States
  const [filteredRequests, setFilteredRequests] = useState([]);

  const [actionStatus, setActionStatus] = useState(null);


 
// Initialize filteredRequests when data loads

  useEffect(() => {

    setFilteredRequests(requests);

  }, [requests]);
  
  // Instant filter function

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

  
  // Initialize filteredRequests when requests change
  useEffect(() => {
    setRequests(requests);
  }, [requests]);
  

  const fetchRequests = async (autoOpen = true) => {
    setLoadingRequests(true);
    setErrorRequests(null);

    // ⏱️ 10-sec timeout safeguard
    const controller = new AbortController();
    const timeoutId = setTimeout(() => {
      controller.abort();
      setLoadingRequests(false);
      setErrorRequests('⏱️ Request timed out. Please try again later.');
    }, 10000);

    //fetch All Rate Requests
    try {
      const res = await withTimeout(fetchAllRateRequests({ signal: controller.signal }));
      clearTimeout(timeoutId);

      const sortedRequests = [...res.data].sort((a, b) => {
        const dateA = a.requestedAt ? new Date(a.requestedAt).getTime() : 0;
        const dateB = b.requestedAt ? new Date(b.requestedAt).getTime() : 0;
        return dateB - dateA;
      });
      setRequests(sortedRequests);
      setErrorRequests(null);

     // 🔹 Auto-open modal only if there’s a latest IN_PROGRESS request
// ✅ Auto-open only if not suppressed
    if (autoOpen && !suppressAutoOpen && sortedRequests.length > 0) {
      const latestInProgress = sortedRequests.find(
        (req) => req.status === 'IN_PROGRESS'
      );

    if (latestInProgress) {
      setSelectedRequest(latestInProgress);
      setShowModal(true);
      setDecisionComment('');
      setModalMessage(null);
      setLockActions(false);
      setCurrentModalPage(1);
    } else {
      // Optional: show info message if no IN_PROGRESS found
      console.log('No IN_PROGRESS requests found.');
    }
  }
    } catch (err) {
      if (err.name === 'AbortError') {
        console.warn('Fetch aborted due to timeout.');
      } else {
        console.error(err);
        setErrorRequests('❌ Failed to load requests. Please retry.');
      }
    } finally {
      clearTimeout(timeoutId);
      setLoadingRequests(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleOpenRequest = (request) => {
    setSelectedRequest(request);
    setShowModal(true);
    setDecisionComment('');
    setModalMessage(null);
    setLockActions(false); // reset on open
    setCurrentModalPage(1);
  };

  const handleCloseModal = () => {
    setSelectedRequest(null);
    setShowModal(false);
  };

  //Checker Decision with APPROVE or REJECTED
  // Helper 1 - validate rejection and confirmation
  const validateRejection = async (status, decisionComment) => {
    let result = 0;
    // REJECTED case
    if (status === "REJECTED") {
      if (decisionComment.trim() === "") {
        await Swal.fire({
          icon: "warning",
          title: "Missing Comment",
          text: "Please provide a comment before rejecting this request.",
          confirmButtonText: "OK",
          confirmButtonColor: "#3085d6"
        });
        result = 1;
      } else {
        const confirmed = await Swal.fire({
          title: "Reject Request?",
          text: "Are you sure you want to reject this request?",
          icon: "question",
          showCancelButton: true,
          confirmButtonText: "Yes, Reject",
          cancelButtonText: "No, Cancel",
          confirmButtonColor: "#d33", // red button
          cancelButtonColor: "#6c757d" // gray button
        });

        if (!confirmed.isConfirmed) {
          console.log("User cancelled rejection");
          result = 1; // stop flow
        }
      }

    // APPROVED case
    } else if (status === "APPROVED") {
      const confirmed = await Swal.fire({
        title: "Approve Request?",
        text: "Are you sure you want to approve this request?",
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Yes, Approve",
        cancelButtonText: "No, Cancel",
        confirmButtonColor: "#198754", // green button
        cancelButtonColor: "#6c757d" // gray button
      });

      if (!confirmed.isConfirmed) {
        console.log("User cancelled approval");
        result = 1; // stop flow
      }
    }
    return result; // 0 = OK, 1 = STOP
  };

  // Helper 2 - process the decision via API
  const processDecision = async (status, selectedRequest, username, decisionComment) => {
    await approveRejectRateRequest(selectedRequest.id, username, {
      coment: decisionComment,
      status: status,
    });

    console.log("Btn status before : " + status);

    if (status === "APPROVED") {
      const response = await updateOutputCurrencyUnits(selectedRequest.rateItems);
      console.log("Response:", response.data);

      if (response?.data !== null) {
        return `✅ Request Approved & Tables ${response?.data || ""} Success.`;
      } else {
        return `❌ Approval Request Failed. Try Again Later.`;
      }
    } else if (status === "REJECTED") {
      return "✅ Request Rejected Success.";
    }

    return "";
  };

  // Helper 3 - finalize and update UI
  const finalizeDecision = (status, setSelectedRequest, setLockActions, fetchRequests) => {
    console.log("Btn status after : " + status);
    setSelectedRequest((prev) => ({ ...prev, status }));
    setLockActions(true);
    setSuppressAutoOpen(true); // 🚫 prevent modal reopen
    setTimeout(() => {
      fetchRequests(false); // 👈 explicitly disable autoOpen
      setSuppressAutoOpen(false); // 🔄 reset for future normal use
    }, 3500);
  };

  // Main handler
  const handleDecision = async (status) => {
    // ✅ Prevent multiple clicks instantly
    if (isSubmittingDecision) return;

    setIsSubmittingDecision(true); // disable buttons + show spinner
    setLockActions(true); // disable textarea immediately

    try {
      console.log("Comment : " + decisionComment);
      const result = await validateRejection(status, decisionComment);

      // only continue if validation passes
      if (result === 0) {
        const message = await processDecision(status, selectedRequest, username, decisionComment);
        setModalMessage(message);

        finalizeDecision(status, setSelectedRequest, setLockActions, fetchRequests);
      } else {
        // 🧩 Re-enable if validation blocked flow (like user canceled reject)
        setIsSubmittingDecision(false);
        setLockActions(false);
      }
    } catch (err) {
      console.error(err);
      setModalMessage(`❌ System Error. Failed to Proceed`);
      setLockActions(true);
    } finally {
      // ✅ Keep lockActions = true after decision; stop spinner
      setIsSubmittingDecision(false);
    }
  };

  // -------- Group rateItems ----------
  const getGroupedRateItems = () => {
    if (!selectedRequest || !selectedRequest.rateItems) return [];
      const grouped = {};
      selectedRequest.rateItems.forEach((item) => { 
      // grouping by fixed currency (kept as-is)
        if (!grouped[item.fixCurrencyCode]) {
          grouped[item.fixCurrencyCode] = {
          fixCurrencyCode: item.fixCurrencyCode,
          varCurrencyCode: item.varCurrencyCode,
          RE_TTBUY: '',
          RE_ODBUY: '',
          RE_TTSEL: '',
          RE_BYOVC: '',
          _CBCL: null, // L
          _CBCS: null // S
          };
        }

        switch (item.rateCode) {
          case 'TTBY':
          grouped[item.fixCurrencyCode].RE_TTBUY = item.varCurnyUnit;
          break;
          case 'ODBY':
          grouped[item.fixCurrencyCode].RE_ODBUY = item.varCurnyUnit;
          break;
          case 'TTSL':
          grouped[item.fixCurrencyCode].RE_TTSEL = item.varCurnyUnit;
          break;
          case 'CBCL': // L
          grouped[item.fixCurrencyCode]._CBCL = item.varCurnyUnit;
          break;
          case 'CBCS': // S
          grouped[item.fixCurrencyCode]._CBCS = item.varCurnyUnit;
          break;
          default:
          break;
        }
    });
    // Build final rows; only when FIXED = USD -> "S - âƒ | L - âƒ"
    return Object.values(grouped).map((row) => {
    if (norm(row.fixCurrencyCode) === 'USD') {
      row.RE_BYOVC = buildBYOVC_USD(row._CBCS, row._CBCL);
    } else {
    // Non-USD: original behavior (CBCL only if present)
      row.RE_BYOVC = row._CBCL ?? row.RE_BYOVC ?? '';
    }
    delete row._CBCL;
    delete row._CBCS;
    return row;
    });
  };



  // -------- Paginate ----------
  const getPaginatedGroupedRateItems = () => {
    const groupedItems = getGroupedRateItems();
    const start = (currentModalPage - 1) * itemsPerModalPage;
    const end = currentModalPage * itemsPerModalPage;
    return groupedItems.slice(start, end);
  };

  const totalModalPages = Math.ceil(getGroupedRateItems().length / itemsPerModalPage);

  return (
    <Container
      fluid
      className="pt-5"
      style={{
        paddingLeft: '300px',
        backgroundColor: '#f8f9fa',
        minHeight: '100vh',
        marginTop: '60px'
      }}
    >
      <Row className="mb-4">
        <Col>
          <h5 className="text-primary fw-bold text-center">Rate Requests - Checker</h5>
        </Col>
      </Row>

      {/* Requests Table */}
      <Row className="justify-content-center">
        <Col md={10}>
          <Card className="shadow rounded-4 border-0">
            <Card.Header className="bg-primary text-white fw-semibold fs-10 rounded-top">
              Submitted Rate Requests
            </Card.Header>
            <Card.Body className="p-4">
              {errorRequests && <Alert variant="danger">{errorRequests}</Alert>}

              {actionStatus && (
            <Alert

                  variant={actionStatus.type}

                  dismissible

                  onClose={() => setActionStatus(null)}
            >

                  {actionStatus.message}
            </Alert>

              )}
            
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
            <p>Loading requests...</p>

              ) : filteredRequests.length === 0 ? (
            <p>No requests found for selected date.</p>

              ) : (
            <div style={{ overflowY: 'auto', maxHeight: '538px' }}>
            <Table striped bordered hover responsive="sm">
            <thead>
            <tr>
            <th>ID</th>
            <th>Requested At</th>
            <th>Requested By</th>
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
            <td>{req.requestedBy}</td>
            <td>{req.status}</td>
            <td>
            <Button

                              size="sm"

                              variant="primary"

                              onClick={() => handleOpenRequest(req)}
            >

                              Review
            </Button>
            </td>
            </tr>

                      ))}
            </tbody>
            </Table>
            </div>

              )}
            </Card.Body>

          </Card>
        </Col>
      </Row>

      {/* Popup Modal for Approve or Reject*/}
      <Modal show={showModal} onHide={handleCloseModal} size="lg" centered>
        <Modal.Header closeButton className="bg-primary text-white fw-semibold fs-10 rounded-top">
          <Modal.Title>
            <h5>Rate Request Details</h5>
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {modalMessage && (
            <Alert
              variant={modalMessage.startsWith('❌') ? 'danger' : 'success'}
              className="mt-2"
            >
              {modalMessage}
            </Alert>
          )}

          {selectedRequest && (
            <>
              <p>
                <strong>Requested By:</strong> {selectedRequest.requestedBy}
              </p>
              <p>
                <strong>Requested At:</strong>{' '}
                {selectedRequest.requestedAt
                  ? format(new Date(selectedRequest.requestedAt), 'yyyy-MM-dd HH:mm')
                  : 'N/A'}
              </p>
              <p>
                <strong>Status:</strong> {selectedRequest.status}
              </p>

              <Table striped bordered hover responsive>
                <thead>
                  <tr>
                    <th rowSpan="2" className="align-middle text-center">
                      FIXED CURRENCY<br></br>CODE
                    </th>
                    {/* <th rowSpan="2" className="align-middle text-center">
                      VAR CURRENCY<br></br>CODE
                    </th> */}
                    <th colSpan="3" className="text-center">
                      EXCHANGE RATES
                    </th>
                    <th colSpan="2" className="text-center">
                      CURRENCY RATES
                    </th>
                  </tr>
                  <tr>
                    <th className="text-center">T/T BUYING<br></br>RATE</th>
                    <th className="text-center">O/D BUYING<br></br>RATE</th>
                    <th className="text-center">T/T SELLING<br></br>RATE</th>
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
                      <td colSpan="7" className="text-center">
                        ❌ No rate items found.
                      </td>
                    </tr>
                  )}
                </tbody>
              </Table>

              {getGroupedRateItems().length > itemsPerModalPage && (
                <Pagination className="justify-content-center mt-3">
                  <Pagination.First
                    onClick={() => setCurrentModalPage(1)}
                    disabled={currentModalPage === 1}
                  />
                  <Pagination.Prev
                    onClick={() =>
                      setCurrentModalPage((prev) => Math.max(prev - 1, 1))
                    }
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
                    onClick={() =>
                      setCurrentModalPage((prev) =>
                        Math.min(prev + 1, totalModalPages)
                      )
                    }
                    disabled={currentModalPage === totalModalPages}
                  />
                  <Pagination.Last
                    onClick={() => setCurrentModalPage(totalModalPages)}
                    disabled={currentModalPage === totalModalPages}
                  />
                </Pagination>
              )}

              <Form.Group className="mt-3">
                <Form.Label>Comment (optional)</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  value={decisionComment || selectedRequest.coment}
                  onChange={(e) => setDecisionComment(e.target.value)}
                  disabled={lockActions} // ✅ disable textarea after decision
                />
              </Form.Group>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button
            variant="success"
            onClick={() => handleDecision('APPROVED')}
            disabled={
              lockActions ||
              isSubmittingDecision ||
              selectedRequest?.status !== 'IN_PROGRESS'
            }
          >
            {isSubmittingDecision && selectedRequest?.status === 'IN_PROGRESS' ? (
              <>
                <Spinner animation="border" size="sm" className="me-2" /> Processing...
              </>
            ) : (
              'Approve'
            )}
          </Button>

          <Button
            variant="danger"
            onClick={() => handleDecision('REJECTED')}
            disabled={
              lockActions ||
              isSubmittingDecision ||
              selectedRequest?.status !== 'IN_PROGRESS'
            }
          >
            {isSubmittingDecision && selectedRequest?.status === 'IN_PROGRESS' ? (
              <>
                <Spinner animation="border" size="sm" className="me-2" /> Processing...
              </>
            ) : (
              'Reject'
            )}
          </Button>
          <Button variant="secondary" onClick={handleCloseModal}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default CheckerPage;

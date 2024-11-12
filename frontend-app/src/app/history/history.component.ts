import {Component, OnInit} from '@angular/core';
import {HttpEventType} from "@angular/common/http";
import {HistoryRecord, HistoryService} from "../services/history.service";
import {DatePipe, NgClass, NgForOf} from "@angular/common";
import Swal from "sweetalert2";


@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    NgForOf,
    DatePipe,
    NgClass
  ],
  templateUrl: './history.component.html',
  styleUrl: './history.component.css'
})
export class HistoryComponent implements OnInit {
  historyRecords: HistoryRecord[] = [];
  totalPages: number = 0;
  paginatedHistory: HistoryRecord[] = [];
  itemsPerPage: number = 10;
  currentPage: number = 1;
  sortDirection: 'asc' | 'desc' = 'desc';

  pageText = $localize`:@@pageText:Page ${this.currentPage} of ${this.totalPages}`;

  statusTranslations: { [key: string]: string } = {
    pending: $localize`:@@statusPending:Pending`,
    completed: $localize`:@@statusCompleted:Completed`,
    canceled: $localize`:@@statusCanceled:Canceled`
  };

  constructor(private historyService: HistoryService) {
  }

  ngOnInit() {
    this.historyService.loadHistory().subscribe((records) => {
      this.historyRecords = records;
      this.totalPages = Math.ceil(this.historyRecords.length / this.itemsPerPage);
      this.paginateHistory();
    });
  }

  paginateHistory() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    this.paginatedHistory = this.historyRecords.slice(start, start + this.itemsPerPage);
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.paginateHistory();
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.paginateHistory();
    }
  }

  getStatusTranslation(status: string) {
    return this.statusTranslations[status.toLowerCase()] || status;
  }

  sortData(column: keyof HistoryRecord) {
    this.sortDirection = this.sortDirection === 'desc' ? 'asc' : 'desc';
    this.historyRecords.sort((a, b) => {
      const valA = a;
      const valB = b;

      return this.sortDirection === 'asc'
        ? (valA > valB ? 1 : -1)
        : (valA < valB ? 1 : -1);
    });
    this.paginateHistory();
  }

  downloadComparison(batchId: string) {
    this.historyService.downloadComparison(batchId).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.Response) {
          const blob: Blob = new Blob([event.body], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'});
          const url = window.URL.createObjectURL(blob);

          const a = document.createElement('a');
          a.href = url;
          const contentDisposition: string | null = event.headers.get('content-disposition');
          let fileName: string = "comparison_report.xlsx";
          if (!contentDisposition) {
            return;
          }
          const index = contentDisposition.indexOf("=");

          if (index > -1) {
            fileName = contentDisposition.substring(index + 1);
          }

          a.download = fileName;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);

          Swal.fire({
            title: 'Successfully downloaded',
            text:  'The comparison record has been downloaded successfully!',
            icon: 'success',
            showConfirmButton: false,
            showCloseButton: true,
            background: '#ffffff'
          })
        }
      }
    })
  }
}

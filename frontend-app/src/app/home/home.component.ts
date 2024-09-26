import {Component, Renderer2, OnInit} from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  constructor(private renderer: Renderer2) {}

  ngOnInit(): void {
    const leftSide = this.renderer.selectRootElement('#left-side', true);
    const rightSide = this.renderer.selectRootElement('#right-side', true);

    const handleMove = (event: MouseEvent) => {
      const mouseX = event.clientX;
      const windowWidth = window.innerWidth;

      const leftWidthPercentage = (mouseX / windowWidth) * 100;
      // const rightWidthPercentage = 100 - leftWidthPercentage;

      leftSide.style.width = `${leftWidthPercentage}%`;
      // rightSide.style.width = `${rightWidthPercentage}%`;
    };

    // Listen for mousemove events and adjust the width of the sides
    document.onmousemove = handleMove;

    // Optional: for touch devices, you can handle touch movement similarly
    // @ts-ignore
    document.ontouchmove = (e) => handleMove(e.touches[0]);
  }
}
